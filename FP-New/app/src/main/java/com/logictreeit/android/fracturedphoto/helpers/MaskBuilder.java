package com.logictreeit.android.fracturedphoto.helpers;

import java.util.ArrayList;
import java.util.List;
import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.utils.PuzzlePatternType;

public class MaskBuilder {

	public static List<Mask> loadMasks(int patterntype, float wScaleFactor, float hScaleFactor) {
		ArrayList<Mask> masksList = new ArrayList<Mask>();

		if(patterntype == PuzzlePatternType.PATTERN_TYPE_1.getPatternType()){
			masksList.add(new Mask("mask_1", R.drawable.p1_mask_1, 0, 0, 52, 78, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_2", R.drawable.p1_mask_2, 5, 0, 119, 131, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_3", R.drawable.p1_mask_3, 91, 0, 167, 129, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_4", R.drawable.p1_mask_4, 128, 0, 188, 228, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_5", R.drawable.p1_mask_5, 166, 0, 260, 228, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_6", R.drawable.p1_mask_6, 232, 0, 342, 114, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_7", R.drawable.p1_mask_7, 308, 0, 458, 117, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_8", R.drawable.p1_mask_8, 437, 0, 542, 116, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_9", R.drawable.p1_mask_9, 384, 33, 488, 118, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_10", R.drawable.p1_mask_10, 540, 0, 640, 119, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_11", R.drawable.p1_mask_11, 0, 78, 31, 246, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_12", R.drawable.p1_mask_12, 30, 40, 190, 229, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_13", R.drawable.p1_mask_13, 246, 106, 398, 250, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_14", R.drawable.p1_mask_14, 311, 117, 520, 263, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_15", R.drawable.p1_mask_15, 397, 114, 559, 216, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_16", R.drawable.p1_mask_16, 531, 114, 640, 263, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_17", R.drawable.p1_mask_17, 0, 129, 126, 388, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_18", R.drawable.p1_mask_18, 125, 189, 188, 300, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_19", R.drawable.p1_mask_19, 126, 205, 260, 422, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_20", R.drawable.p1_mask_20, 255, 243, 464, 345, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_21", R.drawable.p1_mask_21, 436, 134, 531, 310, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_22", R.drawable.p1_mask_22, 519, 134, 590, 306, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_23", R.drawable.p1_mask_23, 20, 239, 126, 452, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_24", R.drawable.p1_mask_24, 44, 299, 244, 451, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_25", R.drawable.p1_mask_25, 243, 244, 383, 423, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_26", R.drawable.p1_mask_26, 393, 294, 482, 387, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_27", R.drawable.p1_mask_27, 465, 294, 574, 407, wScaleFactor, hScaleFactor, true));//
			masksList.add(new Mask("mask_28", R.drawable.p1_mask_28, 552, 197, 640, 442, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_29", R.drawable.p1_mask_29, 0, 332, 45, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_30", R.drawable.p1_mask_30, 24, 421, 252, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_31", R.drawable.p1_mask_31, 243, 371, 380, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_32", R.drawable.p1_mask_32, 329, 335, 477, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_33", R.drawable.p1_mask_33, 380, 334, 516, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_34", R.drawable.p1_mask_34, 514, 309, 574, 442, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_35", R.drawable.p1_mask_35, 514, 366, 640, 480, wScaleFactor, hScaleFactor, false));

		}else if(patterntype == PuzzlePatternType.PATTERN_TYPE_2.getPatternType()){
			masksList.add(new Mask("mask_1", R.drawable.p2_mask_1, 0, 0, 59, 225, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_2", R.drawable.p2_mask_2, 9, 0, 142, 205, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_3", R.drawable.p2_mask_3, 76, 1, 175, 268, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_4", R.drawable.p2_mask_4, 141, 0, 224, 144, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_5", R.drawable.p2_mask_5, 129, 0, 312, 242, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_6", R.drawable.p2_mask_6, 277, 0, 418, 133, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_7", R.drawable.p2_mask_7, 323, 0, 456, 137, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_8", R.drawable.p2_mask_8, 423, 0, 565, 201, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_9", R.drawable.p2_mask_9, 551, 0, 640, 153, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_10", R.drawable.p2_mask_10, 0, 172, 117, 413, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_11", R.drawable.p2_mask_11, 109, 111, 228, 301, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_12", R.drawable.p2_mask_12, 204, 115, 311, 342, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_13", R.drawable.p2_mask_13, 227, 44, 324, 342, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_14", R.drawable.p2_mask_14, 310, 132, 386, 340, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_15", R.drawable.p2_mask_15, 358, 99, 483, 282, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_16", R.drawable.p2_mask_16, 484, 33, 566, 277, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_17", R.drawable.p2_mask_17, 553, 55, 640, 240, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_18", R.drawable.p2_mask_18, 0, 284, 116, 480 , wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_19", R.drawable.p2_mask_19, 101, 270, 188, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_20", R.drawable.p2_mask_20, 150, 295, 284, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_21", R.drawable.p2_mask_21, 259, 321, 334, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_22", R.drawable.p2_mask_22, 310, 196, 494, 341, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_23", R.drawable.p2_mask_23, 309, 289, 453, 397, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_24", R.drawable.p2_mask_24, 321, 368, 431, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_25", R.drawable.p2_mask_25, 358, 274, 494, 480, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_26", R.drawable.p2_mask_26, 493, 194, 640, 354, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_27", R.drawable.p2_mask_27, 448, 371, 553, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_28", R.drawable.p2_mask_28, 472, 278, 592, 480, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_29", R.drawable.p2_mask_29, 564, 266, 640, 480, wScaleFactor, hScaleFactor, false));

		}else if(patterntype == PuzzlePatternType.PATTERN_TYPE_3.getPatternType()){
			masksList.add(new Mask("mask_1", R.drawable.p3_mask_1, 0, 0, 156, 179, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_2", R.drawable.p3_mask_2, 131, 00, 275, 122, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_3", R.drawable.p3_mask_3, 133, 00, 307, 212, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_4", R.drawable.p3_mask_4, 276, 00, 361, 207, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_5", R.drawable.p3_mask_5, 319, 00, 510, 157 , wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_6", R.drawable.p3_mask_6, 455, 00, 607, 234, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_7", R.drawable.p3_mask_7, 546, 00, 640, 157, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_8", R.drawable.p3_mask_8, 0, 122, 68, 293, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_9", R.drawable.p3_mask_9, 0, 105, 169, 350, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_10", R.drawable.p3_mask_10, 306, 39, 462, 248, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_11", R.drawable.p3_mask_11, 361, 113, 538, 265, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_12", R.drawable.p3_mask_12, 546, 96, 640, 263, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_13", R.drawable.p3_mask_13, 89, 196, 216, 369, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_14", R.drawable.p3_mask_14, 203, 195, 372, 417, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_15", R.drawable.p3_mask_15, 362, 247, 538, 351, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_16", R.drawable.p3_mask_16, 533, 157, 587, 361, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_17", R.drawable.p3_mask_17, 204, 201, 321, 416, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_18", R.drawable.p3_mask_18, 309, 276, 377, 429, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_19", R.drawable.p3_mask_19, 369, 276, 501, 447, wScaleFactor, hScaleFactor, true));
			masksList.add(new Mask("mask_20", R.drawable.p3_mask_20, 551, 201, 640, 450, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_21", R.drawable.p3_mask_21, 00, 325, 72, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_22", R.drawable.p3_mask_22, 28, 307, 216, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_23", R.drawable.p3_mask_23, 100, 368, 290, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_24", R.drawable.p3_mask_24, 260, 395, 379, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_25", R.drawable.p3_mask_25, 369, 278, 473, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_26", R.drawable.p3_mask_26, 451, 249, 556, 480, wScaleFactor, hScaleFactor, false));
			masksList.add(new Mask("mask_27", R.drawable.p3_mask_27, 533,337, 640, 480, wScaleFactor, hScaleFactor, false));
		}
		return masksList;
	}

}
