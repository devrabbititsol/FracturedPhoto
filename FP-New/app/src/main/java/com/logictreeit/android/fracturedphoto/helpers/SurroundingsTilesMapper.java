package com.logictreeit.android.fracturedphoto.helpers;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;

import com.logictreeit.android.fracturedphoto.utils.PuzzlePatternType;

public class SurroundingsTilesMapper {

	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, int[]> mappings = new HashMap<Integer, int[]>();

	private static void doPrepareMappings(int patternType) {

		if(patternType == PuzzlePatternType.PATTERN_TYPE_1.getPatternType()){
			mappings.put(0, new int[] {1, 10});
			mappings.put(1, new int[] {0, 2, 10, 11});
			mappings.put(2, new int[] {1, 3, 11});
			mappings.put(3, new int[] {2, 4, 11});
			mappings.put(4, new int[] {3, 5, 12, 18});
			mappings.put(5, new int[] {4, 6, 12});
			mappings.put(6, new int[] {5, 7, 8, 12});
			mappings.put(7, new int[] {6, 8, 9, 14});
			mappings.put(8, new int[] {6, 7, 12, 14});
			mappings.put(9, new int[] {7, 14, 15});
			mappings.put(10, new int[] {0, 1, 16});
			mappings.put(11, new int[] {1, 2, 3, 16, 17});
			mappings.put(12, new int[] {4, 5, 6, 8, 13, 18, 19});
			mappings.put(13, new int[] {12, 14, 19, 20});
			mappings.put(14, new int[] {7, 8, 13, 15, 9, 20});
			mappings.put(15, new int[] {9, 14, 21, 27});
			mappings.put(16, new int[] {10, 11, 17, 22, 28});
			mappings.put(17, new int[] {11, 16, 18});
			mappings.put(18, new int[] {4, 12, 17, 23, 24});
			mappings.put(19, new int[] {24, 31, 25, 13, 20, 12});
			mappings.put(20, new int[] {13, 14, 19, 21, 25, 26});
			mappings.put(21, new int[] {20, 27, 26, 15});
			mappings.put(22, new int[] {16, 23, 28});
			mappings.put(23, new int[] {18, 22, 29});
			mappings.put(24, new int[] {19, 30, 31, 18});
			mappings.put(25, new int[] {32, 26, 19, 20});
			mappings.put(26, new int[] {25, 33, 20, 21, 27, 32});
			mappings.put(27, new int[] {21, 26, 34, 33, 15});
			mappings.put(28, new int[] {29, 22, 16});
			mappings.put(29, new int[] {23, 28, 30});
			mappings.put(30, new int[] {29, 31, 24});
			mappings.put(31, new int[] {19, 24, 30, 32});
			mappings.put(32, new int[] {25, 26, 31, 33, 34});
			mappings.put(33, new int[] {26, 27, 32, 34});
			mappings.put(34, new int[] {27, 32, 33});

		}else if(patternType == PuzzlePatternType.PATTERN_TYPE_2.getPatternType()){
			mappings.put(0, new int[] {1, 9});
			mappings.put(1, new int[] {0, 2, 9});
			mappings.put(2, new int[] {1, 3, 9, 4, 10});
			mappings.put(3, new int[] {2, 4});
			mappings.put(4, new int[] {3, 5, 2, 10, 12});
			mappings.put(5, new int[] {4, 6, 12});
			mappings.put(6, new int[] {5, 7, 13, 14});
			mappings.put(7, new int[] {6, 8, 15, 14});
			mappings.put(8, new int[] {7, 15, 16});
			mappings.put(9, new int[] {0, 1, 2, 17, 18});
			mappings.put(10, new int[] {2, 4, 11, 18, 19});
			mappings.put(11, new int[] {10, 12, 19, 20});
			mappings.put(12, new int[] {4, 5, 11, 13});
			mappings.put(13, new int[] {6, 12, 14, 21});
			mappings.put(14, new int[] {7, 6, 13, 21});
			mappings.put(15, new int[] {7, 8, 16, 21, 25});
			mappings.put(16, new int[] {15, 25, 8});
			mappings.put(17, new int[] {9, 18});
			mappings.put(18, new int[] {9, 10, 17, 19});
			mappings.put(19, new int[] {10, 11, 18, 20});
			mappings.put(20, new int[] {11, 19, 22, 23});
			mappings.put(21, new int[] {13, 14, 15, 22, 24});
			mappings.put(22, new int[] {20, 21, 23, 24});
			mappings.put(23, new int[] {20, 22, 24});
			mappings.put(24, new int[] {21, 22, 23, 26, 27});
			mappings.put(25, new int[] {15, 16, 27, 28});
			mappings.put(26, new int[] {24, 27});
			mappings.put(27, new int[] {25, 26, 28});
			mappings.put(28, new int[] {25, 27});

		}else if(patternType == PuzzlePatternType.PATTERN_TYPE_3.getPatternType()){
			mappings.put(0, new int[] {1, 7, 8});
			mappings.put(1, new int[] {0, 2, 8});
			mappings.put(2, new int[] {1, 3, 8, 12, 13});
			mappings.put(3, new int[] {2, 4, 9});
			mappings.put(4, new int[] {3, 5, 9});
			mappings.put(5, new int[] {4, 6, 9, 10, 15});
			mappings.put(6, new int[] {5, 11});
			mappings.put(7, new int[] {0, 8});
			mappings.put(8, new int[] {0, 7, 20, 21, 12, 1, 2});
			mappings.put(9, new int[] {3, 4, 5, 10, 13});
			mappings.put(10, new int[] {5, 9, 14, 15});
			mappings.put(11, new int[] {6, 15, 19});
			mappings.put(12, new int[] {2, 8, 16, 21});
			mappings.put(13, new int[] {2, 16, 23, 17, 9, 14});
			mappings.put(14, new int[] {10, 13, 18, 25});
			mappings.put(15, new int[] {5, 10, 11, 19, 25});
			mappings.put(16, new int[] {12, 13, 22, 23});
			mappings.put(17, new int[] {13, 23, 24});
			mappings.put(18, new int[] {14, 25, 24});
			mappings.put(19, new int[] {11, 15, 25, 26});
			mappings.put(20, new int[] {8, 21});
			mappings.put(21, new int[] {20, 22, 8, 12});
			mappings.put(22, new int[] {16, 23, 21});
			mappings.put(23, new int[] {22, 13, 16, 17, 24});
			mappings.put(24, new int[] {17, 23, 18, 25});
			mappings.put(25, new int[] {19, 26, 14, 15, 18, 24});
			mappings.put(26, new int[] {19, 25}); 
		}
	}

	public static ArrayList<Integer> getSurroundingTiles(int patternType, int tileId) {

		doPrepareMappings(patternType); 
		int associations[] = mappings.get(Integer.valueOf(tileId));
		ArrayList<Integer> surroundingsList = new ArrayList<Integer>();
		for (int i = 0; i < associations.length; ++i) {
			surroundingsList.add(associations[i]);
		}
		return surroundingsList;
	}
}
