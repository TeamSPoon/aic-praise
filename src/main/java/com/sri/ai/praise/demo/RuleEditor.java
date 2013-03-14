/*
 * Copyright (c) 2013, SRI International
 * All rights reserved.
 * Licensed under the The BSD 3-Clause License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://opensource.org/licenses/BSD-3-Clause
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the aic-expresso nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sri.ai.praise.demo;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.google.common.annotations.Beta;
import com.sri.ai.praise.rules.rsyntaxtextarea.RuleTokenMaker;

@Beta
public class RuleEditor extends JPanel {
	private static final long serialVersionUID = 1L;
	//
	private static final String SYNTAX_STYLE_RULES = "Rule Syntax Style";
	//
	private RuleRSyntaxTextArea textArea;
	private RTextScrollPane editorScrollPane;
	//
	private DefaultHighlightPainter errorPainter = new DefaultHighlightPainter(Color.ORANGE); 
	private Object activeErrorHighlight = null;
	
	public RuleEditor() {
		setLayout(new BorderLayout(0, 0));

		textArea = new RuleRSyntaxTextArea(20, 80);
		textArea.setTabsEmulated(false);
		textArea.setTabSize(4);
		textArea.setCodeFoldingEnabled(false);
		
		// The next four lines of code are the important ones to copy when using
		// RSyntaxTextArea
		// for the rules syntax highlighting.
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping(SYNTAX_STYLE_RULES, RuleTokenMaker.class.getName());
		TokenMakerFactory.setDefaultInstance(atmf);
		textArea.setSyntaxEditingStyle(SYNTAX_STYLE_RULES);

		editorScrollPane = new RTextScrollPane(textArea);
		editorScrollPane.setFoldIndicatorEnabled(false);
		add(editorScrollPane, BorderLayout.CENTER);
		
		postGUISetup();
	}
	
	public String getText() {
		String result = textArea.getText();
		
		return result;
	}
	
	public void setText(String text) {
		textArea.setText(text);
		textArea.setCaretPosition(0);
	}
	
	public boolean isEditable() {
		return textArea.isEditable();
	}
	
	public void setEditable(boolean editable) {
		textArea.setEditable(editable);
	}
	
	public boolean isLineNumbersEnabled() {
		return editorScrollPane.getLineNumbersEnabled();
	}
	
	public void setLineNumbersEnabled(boolean enabled) {
		editorScrollPane.setLineNumbersEnabled(false);
	}
	
	public boolean isHighlightCurrentLine() {
		return textArea.getHighlightCurrentLine();
	}
	
	public void setHighlightCurrentLine(boolean highlight) {
		textArea.setHighlightCurrentLine(highlight);
	}

	public boolean canUndo() {
		return textArea.canUndo();
	}
	
	public void undo() {
		textArea.undoLastAction();
	}
	
	public boolean canRedo() {
		return textArea.canRedo();
	}
	
	public void redo() {
		textArea.redoLastAction();
	}
	
	public void discardAllEdits() {
		textArea.discardAllEdits();
	}
	
	public void indicateErrorAtPosition(int startPosition, int endPosition) {	
		try{
			removeExistingErrorHighlights();
			if (endPosition >= textArea.getDocument().getLength()) {
				endPosition = textArea.getDocument().getLength()-1;
			}
			activeErrorHighlight = textArea.getHighlighter().addHighlight(startPosition, endPosition, errorPainter);
		} catch (BadLocationException ble) {
			// ignore
		}		
	}
	
	public void removeExistingErrorHighlights() {
		if (activeErrorHighlight != null) {
			textArea.getHighlighter().removeHighlight(activeErrorHighlight);
			activeErrorHighlight = null;
		}
	}
	
	public Action getCutAction() {
		return RSyntaxTextArea.getAction(RSyntaxTextArea.CUT_ACTION);
	}
	
	public Action getCopyAction() {
		return RSyntaxTextArea.getAction(RSyntaxTextArea.COPY_ACTION);
	}
	
	public Action getPasteAction() {
		return RSyntaxTextArea.getAction(RSyntaxTextArea.PASTE_ACTION);
	}
	
	public Action getDeleteAction() {
		return RSyntaxTextArea.getAction(RSyntaxTextArea.DELETE_ACTION);
	}
	
	public Action getSelectAllAction() {
		return RSyntaxTextArea.getAction(RSyntaxTextArea.SELECT_ALL_ACTION);
	}
	
	public Action getUndoAction() {
		return RSyntaxTextArea.getAction(RSyntaxTextArea.UNDO_ACTION);
	}
	
	public Action getRedoAction() {
		return RSyntaxTextArea.getAction(RSyntaxTextArea.REDO_ACTION);
	}
	
	//
	// PRIVATE
	//
	private void postGUISetup() {
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				removeExistingErrorHighlights();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				removeExistingErrorHighlights();	
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				removeExistingErrorHighlights();
			}
		});
		
		// Forces the menu to be created.
		textArea.getPopupMenu();
	}
	
	private class RuleRSyntaxTextArea extends RSyntaxTextArea {
		private static final long serialVersionUID = 1L;
	
		private JMenuItem undoMenuItem;
		private JMenuItem redoMenuItem;
		private JMenuItem cutMenuItem;
		private JMenuItem copyMenuItem;
		private JMenuItem pasteMenuItem;
		private JMenuItem deleteMenuItem;
		
		public RuleRSyntaxTextArea(int rows, int cols) {
			super(rows, cols);
			
			RSyntaxTextArea.getAction(RSyntaxTextArea.CUT_ACTION).putValue(Action.SMALL_ICON, ImageLookup.EDIT_CUT_SMALL);
			RSyntaxTextArea.getAction(RSyntaxTextArea.COPY_ACTION).putValue(Action.SMALL_ICON, ImageLookup.EDIT_COPY_SMALL);
			RSyntaxTextArea.getAction(RSyntaxTextArea.PASTE_ACTION).putValue(Action.SMALL_ICON, ImageLookup.EDIT_PASTE_SMALL);
			RSyntaxTextArea.getAction(RSyntaxTextArea.DELETE_ACTION).putValue(Action.SMALL_ICON, ImageLookup.EDIT_DELETE_SMALL);
			RSyntaxTextArea.getAction(RSyntaxTextArea.SELECT_ALL_ACTION).putValue(Action.SMALL_ICON, ImageLookup.EDIT_SELECT_ALL_SMALL);
			
			RSyntaxTextArea.getAction(RSyntaxTextArea.UNDO_ACTION).putValue(Action.SMALL_ICON, ImageLookup.UNDO_SMALL);
			RSyntaxTextArea.getAction(RSyntaxTextArea.UNDO_ACTION).putValue(Action.LARGE_ICON_KEY, ImageLookup.UNDO_LARGE);
			
			RSyntaxTextArea.getAction(RSyntaxTextArea.REDO_ACTION).putValue(Action.SMALL_ICON, ImageLookup.REDO_SMALL);
			RSyntaxTextArea.getAction(RSyntaxTextArea.REDO_ACTION).putValue(Action.LARGE_ICON_KEY, ImageLookup.REDO_LARGE);
		}
		
		@Override
		protected void configurePopupMenu(JPopupMenu popupMenu) {

			boolean canType = isEditable() && isEnabled();

			// Since the user can customize the popup menu, these actions may not
			// have been created.
			if (undoMenuItem != null) {
				undoMenuItem.setEnabled(canUndo() && canType);
			}
			if (redoMenuItem != null) {
				redoMenuItem.setEnabled(canRedo() && canType);
			}
			cutMenuItem.setEnabled(RSyntaxTextArea.getAction(RSyntaxTextArea.CUT_ACTION).isEnabled() && canType);
			copyMenuItem.setEnabled(RSyntaxTextArea.getAction(RSyntaxTextArea.COPY_ACTION).isEnabled());
			pasteMenuItem.setEnabled(RSyntaxTextArea.getAction(RSyntaxTextArea.PASTE_ACTION).isEnabled() && canType);
			deleteMenuItem.setEnabled(RSyntaxTextArea.getAction(RSyntaxTextArea.DELETE_ACTION).isEnabled() && canType);			
		}
		
		@Override
		protected JPopupMenu createPopupMenu() {			
			JPopupMenu menu = new JPopupMenu();
			
			menu.add(undoMenuItem = createPopupMenuItem(RSyntaxTextArea.getAction(RSyntaxTextArea.UNDO_ACTION)));
			menu.add(redoMenuItem = createPopupMenuItem(RSyntaxTextArea.getAction(RSyntaxTextArea.REDO_ACTION)));
			menu.addSeparator();
			menu.add(cutMenuItem = createPopupMenuItem(RSyntaxTextArea.getAction(RSyntaxTextArea.CUT_ACTION)));
			menu.add(copyMenuItem = createPopupMenuItem(RSyntaxTextArea.getAction(RSyntaxTextArea.COPY_ACTION)));
			menu.add(pasteMenuItem = createPopupMenuItem(RSyntaxTextArea.getAction(RSyntaxTextArea.PASTE_ACTION)));
			menu.addSeparator();
			menu.add(deleteMenuItem = createPopupMenuItem(RSyntaxTextArea.getAction(RSyntaxTextArea.DELETE_ACTION)));
			menu.add(createPopupMenuItem(RSyntaxTextArea.getAction(RSyntaxTextArea.SELECT_ALL_ACTION)));			
			
			return menu;
		}


		/**
		 * Creates and configures a menu item for used in the popup menu.
		 *
		 * @param a The action for the menu item.
		 * @return The menu item.
		 * @see #createPopupMenu()
		 */
		protected JMenuItem createPopupMenuItem(Action a) {
			JMenuItem item = new JMenuItem(a) {
				private static final long serialVersionUID = 1L;

				public void setToolTipText(String text) {
					// Ignore!  Actions (e.g. undo/redo) set this when changing
					// their text due to changing enabled state.
				}
			};
			item.setAccelerator(null);
			return item;
		}
	}
}
