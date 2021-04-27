package com.logictreeit.android.fracturedphoto.utils;

public enum PuzzlePatternType {
	
	PATTERN_TYPE_1(0), PATTERN_TYPE_2(1), PATTERN_TYPE_3(2);

	private int patternType;

	private PuzzlePatternType(int patternType) {
		this.patternType = patternType;
	}
	
	public int getPatternType() {
		return patternType;
	}
}
