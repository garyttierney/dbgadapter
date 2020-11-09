package com.github.garyttierney.ghidradbg.plugin.ui

import java.awt.BorderLayout
import javax.swing.*

class DebuggerComponent(val onAttach: () -> Unit) : JPanel() {
    val attachConfiguration = JTextField()

    init {
        layout = BorderLayout()

        val tabs = JTabbedPane()
        add(tabs, BorderLayout.CENTER)

        tabs.addTab("Output", JTextArea())

        val toolbar = JToolBar()
        val attachButton = JButton("attach")

        with(toolbar) {
            add(attachButton)
            add(attachConfiguration)
        }

        attachButton.addActionListener {
            onAttach()
        }

        add(toolbar, BorderLayout.SOUTH)
    }
}