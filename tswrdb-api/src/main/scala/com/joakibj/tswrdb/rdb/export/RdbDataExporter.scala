/*
 * tswrdb - a program and API to export the TSW resource database
 *
 * Copyright (C) 2013 Joakim Bjørnstad <joakibj@gmail.com>
 *
 * Licensed under the GNU General Public License version 2.
 * Please see the LICENSE file for the license text in verbatim.
 */

package com.joakibj.tswrdb.rdb.export

import java.io.File
import com.joakibj.tswrdb.rdb.index.{RdbIndexEntry, RdbIndexFileReader}
import com.joakibj.tswrdb.rdb._
import com.joakibj.tswrdb.rdb.RdbIOException
import scala.Some
import strings.RdbFilenameReader

abstract class RdbDataExporter(val rdbDataDirectory: File) {
  protected val IndexFilename = "le.idx"
  protected val validRdbFileNums = rdbDataDirectory.listFiles.filter(_.getName.endsWith(".rdbdata")).map(_.getName.split("\\.").head.toInt)
  protected val indexTable = getIndexTable()
  protected val fileNameTable = getFilenameTable()
  protected val postDataTransformer: RdbDataTransformer

  def exportAll(rdbType: RdbType) {
    val indexEntries = indexTable.entriesForType(rdbType.id)
    export(rdbType, indexEntries)
  }

  def exportFiltered(rdbType: RdbType, filter: (RdbIndexEntry) => Boolean) {
    val indexEntries = indexTable.entriesForType(rdbType.id).filter(filter)
    export(rdbType, indexEntries)
  }

  protected def export(rdbType: RdbType, indexEntries: Array[RdbIndexEntry]) {
    val groupedIndexEntries = grouped(indexEntries)
    val outputDirectory = createOutputDirectory(rdbType).getOrElse {
      throw new RuntimeException("Unable to create exported directory.")
    }

    groupedIndexEntries.keys.foreach {
      (fileNum) =>
        try {
          exportEntriesFromFileNum(rdbType, outputDirectory, fileNum, groupedIndexEntries(fileNum))
        } catch {
          case ex: RdbIOException => ex match {
            case RdbIOException(msg@_, Severity.Continuable) => {
              println("Recoverable exception occured: " + msg + ". Continuing...")
            }
            case RdbIOException(msg@_, Severity.Mayan) => {
              throw new RuntimeException("Unrecoverable exception occured: " + msg)
            }
          }
          case ex: Throwable =>
            throw new RuntimeException("Unknown unrecoverable exception occured: " + ex.getClass.getName + ": " + ex.getMessage)
        }
    }
  }

  private def createOutputDirectory(rdbType: RdbType): Option[File] = {
    val outputDirectory = new File("./exported/" + rdbType)
    val created = if (!outputDirectory.isDirectory) outputDirectory.mkdirs() else true

    if (created) Some(outputDirectory) else None
  }

  private def exportEntriesFromFileNum(rdbType: RdbType, outputDirectory: File, fileNum: Int, indexEntries: Array[RdbIndexEntry]) {
    if (!validRdbFileNums.contains(fileNum)) throw new RdbIOException("Filenum: " + fileNum + " does not exist")

    val rdbDataFile = new File(rdbDataDirectory, "%02d.rdbdata" format fileNum)
    val rdbDataFileReader = RdbDataFileReader(rdbDataFile, indexEntries)
    val rdbData = rdbDataFileReader.readDataEntries()
    rdbDataFileReader.close()
    exportData(rdbType, outputDirectory, rdbData)

    println("Exporting entries from: " + rdbDataFile.getName)
  }

  private def exportData(rdbType: RdbType, outputDirectory: File, rdbData: Vector[(RdbDataEntry, Array[Byte])]) {
    rdbData.foreach {
      case (entry, buf) =>
        val transformedBuf = postDataTransformer.transform(buf)
        if (transformedBuf.size > 0) {
          exportDataToFile(rdbType, outputDirectory, entry, transformedBuf)
        } else throw new RdbIOException("Entry " + entry.id + " was empty. Skipped write.")
    }
  }

  protected def exportDataToFile(rdbType: RdbType, outputDirectory: File, dataEntry: RdbDataEntry, buf: Array[Byte])

  private def grouped(arr: Array[RdbIndexEntry]): Map[Int, Array[RdbIndexEntry]] =
    arr.groupBy((indexEntry: RdbIndexEntry) => indexEntry.fileNum.toInt)

  private def getIndexTable() = {
    val rdbIndexFileReader = RdbIndexFileReader(new File(rdbDataDirectory, IndexFilename))
    val indexTable = rdbIndexFileReader.getIndexTable
    rdbIndexFileReader.close()

    indexTable
  }

  private def getFilenameTable() = {
    val filenameEntries = indexTable.entriesForType(RdbTypes.Filenames.id)
    if (filenameEntries.size == 1) {
      val rdbDataFile = new File(rdbDataDirectory, "%02d.rdbdata" format filenameEntries(0).fileNum)
      val rdbDataFileReader = RdbDataFileReader(rdbDataFile, filenameEntries)
      val rdbData = rdbDataFileReader.readDataEntries()
      rdbDataFileReader.close()
      val rdbFilenameReader = RdbFilenameReader(rdbData(0)._2)
      val filenameTable = rdbFilenameReader.getFileNames()
      rdbFilenameReader.close()

      filenameTable
    } else
      throw new RdbIOException("Filename entries had 0 or more than 1 entries")
  }
}
