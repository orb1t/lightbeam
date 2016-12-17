/*
 * Copyright (c) 2008-2010 LightBeam Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of LightBeam Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.lightbeam.panels;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.pushingpixels.lightbeam.*;

/**
 * Test application panel for testing {@link JList} component.
 * 
 * @author Kirill Grouchnikov
 */
public class ListPanel extends JPanel {

	/**
	 * List model implementation with support for moving elements.
	 * 
	 * @author Kirill Grouchnikov
	 */
	private static class MoveableListModel extends AbstractListModel {
		/**
		 * The string list backing up the model.
		 */
		protected List<String> model;

		/**
		 * Creates a new model.
		 * 
		 * @param modelSize
		 * 		Model size.
		 */
		public MoveableListModel(int modelSize) {
			super();
			model = new ArrayList<String>();
			for (int i = 0; i < modelSize; i++) {
				model.add("element " + i);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		public Object getElementAt(int index) {
			return model.get(index);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.ListModel#getSize()
		 */
		public int getSize() {
			return model.size();
		}

		/**
		 * Moves the element at the specified index one position up.
		 * 
		 * @param index
		 * 		Element index.
		 */
		public void moveUp(int index) {
			String entry = model.get(index);
			model.set(index, model.get(index - 1));
			model.set(index - 1, entry);
			fireContentsChanged(this, index - 1, index);
		}

		/**
		 * Moves the element at the specified index one position down.
		 * 
		 * @param index
		 * 		Element index.
		 */
		public void moveDown(int index) {
			String entry = model.get(index);
			model.set(index, model.get(index + 1));
			model.set(index + 1, entry);
			fireContentsChanged(this, index, index + 1);
		}

		/**
		 * Deletes the element at the specified index.
		 * 
		 * @param index
		 * 		Element index.
		 */
		public void delete(int index) {
			model.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		/**
		 * Moves an element in the model.
		 * 
		 * @param fromIndex
		 * 		The location of the element to move.
		 * @param toIndex
		 * 		The new location of that element.
		 */
		public void move(int fromIndex, int toIndex) {
			String from = model.remove(fromIndex);
			model.add(toIndex, from);
			fireContentsChanged(this, fromIndex, toIndex);
		}
	}

	/**
	 * List.
	 */
	private JList list;

	/**
	 * Creates a new list panel.
	 */
	public ListPanel() {
		super();
		setLayout(new BorderLayout());
		list = new JList(new MoveableListModel(1000));

		final JScrollPane jsp = new JScrollPane(list);
		this.add(jsp, BorderLayout.CENTER);
	}

	@PerformanceScenarioParticipant
	public PerformanceScenario getLargeScrollScenario() {
		return new PerformanceScenario() {
			int[] perms;

			@Override
			public String getName() {
				return "Scrolling large list";
			}

			@Override
			public void setup() {
				list.scrollRectToVisible(list.getCellBounds(0, 0));
				list.getSelectionModel().clearSelection();
				this.perms = LightBeamUtils.getPermutation(list.getModel()
						.getSize(), this.getIterationCount());
			}

			@Override
			public int getIterationCount() {
				return 10;
			}

			@Override
			public void runSingleIteration(int iterationNumber) {
				list.scrollRectToVisible(list.getCellBounds(
						this.perms[iterationNumber],
						this.perms[iterationNumber]));
				list.paintImmediately(list.getVisibleRect());
			}
		};
	}

	@PerformanceScenarioParticipant
	public PerformanceScenario getLargeMoveScenario() {
		return new PerformanceScenario() {
			int[] perms;

			@Override
			public String getName() {
				return "Moving elements in a large list";
			}

			@Override
			public void setup() {
				list.scrollRectToVisible(list.getCellBounds(0, 0));
				list.getSelectionModel().clearSelection();
				this.perms = LightBeamUtils.getPermutation(list.getModel()
						.getSize(), 2 * this.getIterationCount());
			}

			@Override
			public int getIterationCount() {
				return 10;
			}

			@Override
			public void runSingleIteration(int iterationNumber) {
				MoveableListModel model = (MoveableListModel) list.getModel();
				model.move(this.perms[2 * iterationNumber],
						this.perms[2 * iterationNumber + 1]);
				list.paintImmediately(list.getVisibleRect());
			}
		};
	}
}