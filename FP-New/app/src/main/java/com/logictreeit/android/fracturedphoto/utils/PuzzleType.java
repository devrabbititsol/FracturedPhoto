package com.logictreeit.android.fracturedphoto.utils;

public enum PuzzleType {
	
	SQUARE_PUZZLE("SquarePuzzle"), SHATTERED_PUZZLE("ShatteredPuzzle");

	private String puzzleType;

	private PuzzleType(String puzzleType) {
		this.puzzleType = puzzleType;
	}

	public String getPuzzleType() {
		return puzzleType;
	}
}
