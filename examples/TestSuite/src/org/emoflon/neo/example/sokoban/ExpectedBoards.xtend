package org.emoflon.neo.example.sokoban

class ExpectedBoards {
	static def String singleSokoban(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][@][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String twoSokoban(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][@][ ][ ][ ][ ][ ]
			[ ][ ][ ][@][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String singleBlock(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][$][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String singleBoulder(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][#][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String singleEndPos(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][.][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String validBoard(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][@][ ][ ][.][ ][ ]
			[ ][ ][#][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][$][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String validBoardMoveBlock(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][@][$][ ][.][ ][ ]
			[ ][ ][#][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String validBoardMoveBlockAfterMove(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][@][*][ ][ ]
			[ ][ ][#][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String validBoardMoveBoulder(){
		'''
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][@][#][ ][.][ ][ ]
			[ ][ ][$][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
			[ ][ ][ ][ ][ ][ ][ ][ ]
		'''
	}
	static def String validBoardFileImport(){
		'''
			[ ][ ][#][#][#][#][#]
			[#][#][#][@][ ][.][#]
			[#][ ][$][ ][#][.][#]
			[#][ ][ ][$][$][ ][#]
			[#][.][ ][ ][#][ ][#]
			[#][ ][ ][ ][$][.][#]
			[#][#][#][#][#][#][#]
		'''
	}
	
}
