package org.emoflon.neo.example.sokoban;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.moflon.tutorial.sokobangamegui.controller.IController;
import org.moflon.tutorial.sokobangamegui.controller.NeoController;

//FIXME[Jannik]
@Disabled
class TestSokobanBoardValidation extends ENeoTest {

	private TestView view;
	private IController controller;
	
	@BeforeEach
	public void createView() {
		controller = new NeoController((c) -> {
			view = new TestView(c);
			return view;
		}, 8, 8);
	}
	
	@Test
	public void testSingleSokoban() {
		view.createSokoban(2,2);
		assertEquals(ExpectedBoards.singleSokoban(), view.printBoard());
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testTwoSokoban() {
		view.createSokoban(2,2);
		view.createSokoban(3,3);
		assertEquals(ExpectedBoards.twoSokoban(), view.printBoard());
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testSingleBlock() {
		view.createBlock(5,6);
		assertEquals(ExpectedBoards.singleBlock(), view.printBoard());
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testNoSokoban() {
		view.createBlock(5,6);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testNoBlock() {
		view.createSokoban(2,2);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testNoBoulder() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createEndPos(2,5);
		assertTrue(controller.boardIsValid());
	}
	
	@Test
	public void testNoEndPos() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createBoulder(3,2);
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testSingleBoulder() {
		view.createBoulder(3,2);
		assertEquals(ExpectedBoards.singleBoulder(), view.printBoard());
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testSingleEndPos() {
		view.createEndPos(2,5);
		assertEquals(ExpectedBoards.singleEndPos(), view.printBoard());
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testValidBoard() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		assertEquals(ExpectedBoards.validBoard(), view.printBoard());
		assertTrue(controller.boardIsValid());
	}
	
	@Test
	public void testBoulderOnEndPos() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createBoulder(3,2);
		view.createEndPos(3,2);
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testBlockOnBoulder() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		assertThrows(UnsupportedOperationException.class, () -> view.createBoulder(5,6));
	}
	
	@Test
	public void testSokobanOnBoulder() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createEndPos(2,5);
		assertThrows(UnsupportedOperationException.class, () -> view.createBoulder(2,2));
	}
	
	@Test
	public void testSokobanOnBlock() {
		view.createBlock(2,2);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testValidBoardMoreBlocksLessEndPos() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createBlock(6,6);
		view.createBlock(7,6);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testValidBoardMoreEndPosLessBlocks() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		view.createEndPos(4,5);
		view.createEndPos(6,5);
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testValidBoardEqualEndPosAndBlocks() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createBlock(6,6);
		view.createBlock(7,6);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		view.createEndPos(4,5);
		view.createEndPos(6,5);
		assertTrue(controller.boardIsValid());
	}
	
	@Test
	public void testValidBoardBlockIsInCorner() {
		view.createSokoban(2,2);
		view.createBlock(7,7);
		view.createBlock(0,0);
		view.createBlock(0,7);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		view.createEndPos(4,5);
		view.createEndPos(6,5);
		assertFalse(controller.boardIsValid());
	}
	
	@Test
	public void testValidBoardClearBoardAction() {
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createBlock(6,6);
		view.createBlock(7,6);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		view.createEndPos(4,5);
		view.createEndPos(6,5);
		assertTrue(controller.boardIsValid());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		view.getController().clearBoard();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetNewBoardAction() {
		int width = 8;
		int height = 8;
		view.createSokoban(2,2);
		view.createBlock(5,6);
		view.createBlock(6,6);
		view.createBlock(7,6);
		view.createBoulder(3,2);
		view.createEndPos(2,5);
		view.createEndPos(4,5);
		view.createEndPos(6,5);
		assertTrue(controller.boardIsValid());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		view.getController().newBoard(width, height);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
