package com.logictreeit.android.fracturedphoto.fcm;

import java.io.Serializable;
import java.util.ArrayList;

public class PuzzleCreditMetrics implements Serializable {

    public int getPuzzles_balance() {
        return puzzles_balance;
    }

    public void setPuzzles_balance(int puzzles_balance) {
        this.puzzles_balance = puzzles_balance;
    }

    public ArrayList<String> getPurchased_puzzles() {
        return purchased_puzzles;
    }

    public void setPurchased_puzzles(ArrayList<String> purchased_puzzles) {
        this.purchased_puzzles = purchased_puzzles;
    }

    int puzzles_balance;
    ArrayList<String> purchased_puzzles;
}
