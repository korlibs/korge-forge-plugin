// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.korge.intellij.plugin.util

import com.google.common.cache.CacheBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import kotlinx.coroutines.*
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.*
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import kotlin.math.ceil


class JGridPreviewer<T>(
  items: List<T>,
  imageProvider: (T?) -> Icon? = { null },
  labelProvider: (Int, T?) -> String = { index, it -> "$it" },
  itemSize: Dimension = Dimension(80, 80)
) : JPanel(BorderLayout()) {
  companion object {
    val MAX_WIDTH = 80
    val MAX_HEIGHT = 80
  }

  val labelCache = CacheBuilder.newBuilder()
    .maximumSize(200)
    .expireAfterAccess(10, TimeUnit.MINUTES)
    .build<Int, JLabel>()

  var items: List<T> = items
    set(value) {
      field = value
      table.clearSelection()
      labelCache.invalidateAll()
      refresh()
    }

  var itemSize: Dimension = itemSize
    set(value) {
      field = value
      refresh()
    }
  var imageProvider: (T?) -> Icon? = imageProvider
    set(value) {
      field = value
      refresh()
    }
  var labelProvider: (Int, T?) -> String = labelProvider
    set(value) {
      field = value
      refresh()
    }

  val onOpen = Signal<T?>()

  val totalItems: Int get() = items.size
  private var ncolumns = 6

  fun getIndex(row: Int, column: Int): Int = row * ncolumns + column
  fun getRowColumn(index: Int): Pair<Int, Int> = (index / ncolumns) to (index % ncolumns)

  object OutOfBounds

  fun createTableModel(columns: Int): TableModel {
    ncolumns = columns
    return object : DefaultTableModel(0, columns) {
      override fun getRowCount(): Int = ceil(totalItems.toDouble() / columns.toDouble()).toInt()
      override fun getValueAt(row: Int, column: Int): Any? {
        val index = getIndex(row, column)
        if (index !in 0 until totalItems) return OutOfBounds
        return items.getOrNull(index)
      }
    }
  }

  //val table = object : JBTable(createTableModel(6)) {
  val table = object : JTable(createTableModel(6)) {
    // Sobreescribir el método para deshabilitar la edición de celdas
    override fun isCellEditable(row: Int, column: Int): Boolean {
      return false
    }

    //override fun isCellSelected(row: Int, column: Int): Boolean {
    //    return getIndex(row, column) in 0 until totalItems
    //}

    override fun changeSelection(rowIndex: Int, columnIndex: Int, toggle: Boolean, extend: Boolean) {
      val index = getIndex(rowIndex, columnIndex)
      val inRange = index in 0 until totalItems
      if (inRange) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend)
      }
    }

    init {
      addKeyListener(object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
          if (e.keyCode == KeyEvent.VK_ENTER) {
            val selectedIndex = getIndex(selectedRow, selectedColumn)
            println("onOpen=selectedIndex=$selectedIndex, selectedRow=$selectedRow, selectedColumn=$selectedColumn")
            onOpen(this@JGridPreviewer.items.getOrNull(selectedIndex))
          } else {
            super.keyPressed(e)
          }
        }
      })
      addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
          if (e.clickCount == 2) {
            val row = rowAtPoint(e.point)
            val col = columnAtPoint(e.point)
            if (row >= 0 && col >= 0) {
              // Do something with double-clicked cell
              val selectedIndex = getIndex(row, col)
              onOpen(this@JGridPreviewer.items.getOrNull(selectedIndex))
            }
          }
        }
      })
    }

    val myTableCellRenderer = object : TableCellRenderer {
      //val selectedColor = UIManager.getColor("List.selectionBackground")

      private fun getSelectionColor(): java.awt.Color? {
        val editorColorsManager = EditorColorsManager.getInstance()
        return editorColorsManager.globalScheme.getColor(com.intellij.openapi.editor.colors.EditorColors.SELECTION_BACKGROUND_COLOR)
      }

      val MORE_ICON = AllIcons.Actions.MoreHorizontal.renderCentered(MAX_WIDTH, MAX_HEIGHT)

      override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        val index = getIndex(row, column)
        val component = labelCache.get(index) {
          when (value) {
            OutOfBounds -> JLabel()
            else -> {
              JLabel(this@JGridPreviewer.labelProvider(index, value as? T?)).also { label ->
                label.verticalAlignment = SwingConstants.CENTER
                label.horizontalAlignment = SwingConstants.CENTER
                label.verticalTextPosition = SwingConstants.BOTTOM
                label.horizontalTextPosition = SwingConstants.CENTER
                label.icon = MORE_ICON
                CoroutineScope(Dispatchers.IO).launch {
                  val image = this@JGridPreviewer.imageProvider(value as? T?)
                  if (image != null) {
                    SwingUtilities.invokeLater {
                      label.icon = image
                      table?.repaint()
                    }
                  }
                }
              }
            }
          }
        }
        component.isOpaque = true
        component.background = if (!isSelected) JBColor.WHITE else getSelectionColor()
        //component.isEnabled = value != null
        return component
      }
    }

    override fun getCellRenderer(row: Int, column: Int): TableCellRenderer {
      //println("RENDERER: $row=$row, column=$column")
      return myTableCellRenderer
    }
  }.also { table ->
    table.tableHeader = null
    table.setRowHeight(itemSize.height) // Altura de las filas
    table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
    table.setShowGrid(false) // Ocultar las líneas de la cuadrícula
    table.setCellSelectionEnabled(true) // Habilitar la selección de celdas
    //table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) // Establecer el modo de selección a una sola celda
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION) // Establecer el modo de selección a una sola celda
  }

  val scrollPane = JBScrollPane(table).also  { scrollPane ->
    scrollPane.verticalScrollBar.unitIncrement = 16
    scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
    scrollPane.addComponentListener(object : ComponentAdapter() {
      override fun componentResized(e: ComponentEvent) {
        recreateTableModel()
      }
    })
  }

  private fun recreateTableModel() {
    val selectedIndex = getIndex(table.selectedRow, table.selectedColumn)
    table.model = createTableModel((scrollPane.size.width / itemSize.width).coerceAtLeast(1))
    val (selectedRow, selectedColumn) = getRowColumn(selectedIndex)
    table.changeSelection(selectedRow, selectedColumn, false, true)

  }

  val selectedIndex get() = getIndex(table.selectedRow, table.selectedColumn)
  val selectedItem: T? get() = items.getOrNull(selectedIndex)

  init {
    this.add(scrollPane)
  }

  fun focus() {
    table.requestFocus()
  }

  fun refresh() {
    table.rowHeight = itemSize.height
    //recreateTableModel()
    table.repaint()
  }
}