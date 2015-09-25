package com.raggiadolf.connectfour.game;

import java.util.*;

public class State {

    private char[][] grid;
    private String player;
    private int lastCol;
    private int lastRow;

    private static int[][] evaluationTable = {{3, 4,  5,  7,  5, 4, 3},
                                             {4, 6,  8, 10,  8, 6, 4},
                                             {5, 8, 11, 13, 11, 8, 5},
                                             {5, 8, 11, 13, 11, 8, 5},
                                             {4, 6,  8, 10,  8, 6, 4},
                                             {3, 4,  5,  7,  5, 4, 3}};

    public State() {
        this.grid = new char[][]{
                    {0, 0, 0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0, 0, 0},
                    {0, 0, 0, 0, 0, 0, 0}};
        this.player = "RED";
        this.lastCol = 1;
        this.lastRow = 1;
    }

    public State(String player, char[][] grid, int row, int col) {
        this.grid = grid;
        this.player = player;
        this.lastRow = row;
        this.lastCol = col;
    }

    public State(State that) {
        this.grid = deepCopy(that.grid);
        this.player = that.player;
        this.lastCol = that.lastCol;
        this.lastRow = that.lastRow;
    }

    /**
     * this.lastCol holds the column last dropped into,
     * +1 to offset because the board is 1 indexed while our
     * grid is 0 indexed.
     * @return The move that led to the current state.
     */
    public Integer getLastMove() {
        return this.lastCol + 1;
    }

    /**
     * Loops through the grid and if the top row is empty, dropping
     * into that column is legal.
     * @return A list of moves which are legal in the current state.
     */
    public List<Integer> LegalMoves() {
        List<Integer> moves = new ArrayList<Integer>();

        for(int i = 0; i < 7; i++) {
            if(this.grid[5][i] == 0) {
                moves.add(i);
            }
        }

        return moves;
    }

    /**
     * Checks if all of the top rows are full, if they are, we have a terminal state.
     * Also checks if the current state is a goalstate for the current player.
     * @return boolean value.
     */
    public boolean TerminalTest() {
        boolean isTerminal = true;
        for(int i = 0; i < 7; i++) {
            if(this.grid[5][i] == 0) {
                isTerminal = false;
                break;
            }
        }
        return (isTerminal || GoalTest());
    }

    /**
     * Takes a deep copy of the grid, drops a token into a column
     * switches players for the state, and returns a resulting state.
     * @param col, the column to drop a token into.
     * @return A new state after dropping a token into col.
     */
    public State ResultingState(Integer col) {
        char[][] newGrid = deepCopy(this.grid);

        char token;

        if(this.player.charAt(0) == 'W') {
            token = 'R';
        } else {
            token = 'W';
        }

        int row = 0;
        for(int i = 0; i < 6; i++) {
            if(this.grid[i][col] != 0) {
                row++;
            } else {
                break;
            }
        }

        newGrid[row][col] = token;

        String newPlayer;
        if(token == 'W') {
            newPlayer = "WHITE";
        } else {
            newPlayer = "RED";
        }

        return new State(newPlayer, newGrid, row, col);
    }

    /**
     * Just used to get a deep copy of our current grid when we want to create
     * a resulting state.
     * @param original, the state we want a copy of.
     * @return a deep copy of the original state.
     */
    private static char[][] deepCopy(char[][] original) {
        if(original == null) {
            return null;
        }

        final char[][] result = new char[original.length][];
        for(int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }

        return result;
    }

    /**
     * Checks for vertical/horizontal/diagonal winning conditions, only
     * looking around the place where the last token was dropped.
     * @return true/false depending on if the current state has been won
     * by the last player who dropped a token.
     */
    public boolean GoalTest() {
        char token = this.player.charAt(0);

        /**
         * Vertical check
         */

        int count = 0;
        if(this.lastRow >= 3) {
            for(int i = lastRow; i > lastRow - 4; i--) {
                if(this.grid[i][lastCol] != token) {
                    break;
                } else {
                    count++;
                }
            }
            if(count == 4) {
                return true;
            }
        }

        /**
         * Horizontal check
         */
        boolean isOver = true;
        if(this.lastCol >= 3) {
            for(int i = lastCol - 3; i < 4; i++) {
                for (int j = i; j < i + 4; j++) {
                    if(this.grid[lastRow][j] != token) {
                        isOver = false;
                        break;
                    }
                }
                if(isOver) {
                    return true;
                }
                isOver = true;
            }
        } else {
            for(int i = 0; i <= lastCol; i++) {
                for(int j = i; j < i + 4; j++) {
                    if(this.grid[lastRow][j] != token) {
                        isOver = false;
                        break;
                    }
                }
                if(isOver) {
                    return true;
                }
                isOver = true;
            }
        }

        /**
         * Diagonal check
         */

        int startRow = 0, startCol = 0;
        int i, j;
        isOver = true;

        if(this.lastCol >= this.lastRow){
            if(this.lastRow < 3){
                startRow = 0;
                startCol = this.lastCol - this.lastRow;
            }
            else{
                startRow = this.lastRow - 3;
                startCol = this.lastCol - 3;
            }
        }
        else{
            if(this.lastCol < 3){
                startCol = 0;
                startRow = this.lastRow - this.lastCol;
            }
            else{
                startRow = this.lastRow - 3;
                startCol = this.lastCol - 3;
            }
        }

        int diagCount = 0;

        for ( ; startRow < 3 && startCol < 4 ; startRow++, startCol++) {
            diagCount = 0;
            for (i = startRow, j = startCol; i < startRow + 4 && j < startCol + 4; i++, j++) {
                diagCount++;
                if (this.grid[i][j] != token) {
                    isOver = false;
                    break;
                }
            }
            if (isOver && diagCount == 4) {
                return true;
            }
            isOver = true;
        }

        if(this.lastCol >= (5 - this.lastRow)){
            if(this.lastRow > 2){
                startRow = 5;
                startCol = this.lastCol - (5 - this.lastRow);
            }
            else{
                startRow = this.lastRow + 3;
                startCol = this.lastCol - 3;
            }
        }
        else{
            if(this.lastCol < 3){
                startRow = this.lastRow + this.lastCol;
                startCol = 0;
            }
            else{
                startRow = this.lastRow + 3;
                startCol = this.lastCol - 3;
            }
        }

        for ( ; startRow > 2 && startCol < 4 ; startRow--, startCol++) {
            diagCount = 0;
            for (i = startRow, j = startCol; i > startRow - 4 && j < startCol + 4; i--, j++) {
                diagCount++;
                if (this.grid[i][j] != token) {
                    isOver = false;
                    break;
                }
            }
            if (isOver && diagCount == 4) {
                return true;
            }
            isOver = true;
        }

        return false;
    }

    /**
     * If the state is already a goal, the last player has already won, and we
     * return a negative score to the player who's turn it is to do next.
     * Otherwise we start by evaluating the current board against a pre-defined
     * evaluation board, to see which of the players has the 'better' squares captured.
     * Then we sum up pairs of two, three and four tokens for each of the player and give
     * scores depending on which player has the sequences.
     * @return A score for the board, from the perspective of the player who's
     * turn it is to drop the next token.
     */
    public int eval() {
        int utility = 138;
        int sum = 0;

        if(this.GoalTest()) {
            return -1000;
        }

        char token;

        if(this.player.charAt(0) == 'W') {
            token = 'R';
        } else {
            token = 'W';
        }

        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 7; j++) {
                if(this.grid[i][j] == token) {
                    sum += evaluationTable[i][j];
                } else if(this.grid[i][j] != 0) {
                    sum -= evaluationTable[i][j];
                }
            }
        }

        int redCount = 0, whiteCount = 0;

        for(int k = 0; k < 7; k++){
            for(int l = 0; l < 6; l++){

                /**
                 * Vertical check
                 */

                if(l >= 3) {
                    for(int i = l; i > l - 4; i--) {
                        if(this.grid[i][k] == 'R') {
                            redCount++;
                        } else if(this.grid[i][k] == 'W'){
                            whiteCount++;
                        }
                    }
                    if(whiteCount == 3 && redCount == 0) {
                        if(token == 'R'){
                            sum -= 20;
                        }
                        else{
                            sum += 20;
                        }
                    }
                    else if(whiteCount == 0 && redCount == 3){
                        if(token == 'R'){
                            sum += 20;
                        }
                        else{
                            sum -= 20;
                        }
                    }
                    else if(whiteCount == 2 && redCount == 0) {
                        if(token == 'R'){
                            sum -= 10;
                        }
                        else{
                            sum += 10;
                        }
                    }
                    else if(whiteCount == 0 && redCount == 2){
                        if(token == 'R'){
                            sum += 10;
                        }
                        else{
                            sum -= 10;
                        }
                    }
                    else if(whiteCount == 4 && redCount == 0) {
                        if(token == 'R'){
                            sum -= 1000;
                        }
                        else{
                            sum += 1000;
                        }
                    }
                    else if(whiteCount == 0 && redCount == 4){
                        if(token == 'R'){
                            sum += 1000;
                        }
                        else{
                            sum -= 1000;
                        }
                    }
                }
                else if(l == 2){
                    for(int i = l; i > l - 3; i--) {
                        if(this.grid[i][k] == 'R') {
                            redCount++;
                        } else if(this.grid[i][k] == 'W'){
                            whiteCount++;
                        }
                    }
                    if(whiteCount == 2 && redCount == 0) {
                        if(token == 'R'){
                            sum -= 10;
                        }
                        else{
                            sum += 10;
                        }
                    }
                    else if(whiteCount == 0 && redCount == 2){
                        if(token == 'R'){
                            sum += 10;
                        }
                        else{
                            sum -= 10;
                        }
                    }
                }

                whiteCount = 0;
                redCount = 0;

                /**
                 * Horizontal check
                 */
                if(k == 3) {
                    for(int i = 0; i < 4; i++) {
                        whiteCount = 0;
                        redCount = 0;
                        for (int j = i; j < i + 4; j++) {
                            if(this.grid[l][j] == 'R') {
                                redCount++;
                            } else if(this.grid[l][j] == 'W'){
                                whiteCount++;
                            }
                        }
                        if(whiteCount == 3 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 20;
                            }
                            else{
                                sum += 20;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 3){
                            if(token == 'R'){
                                sum += 20;
                            }
                            else{
                                sum -= 20;
                            }
                        }
                        else if(whiteCount == 2 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 10;
                            }
                            else{
                                sum += 10;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 2){
                            if(token == 'R'){
                                sum += 10;
                            }
                            else{
                                sum -= 10;
                            }
                        }
                        else if(whiteCount == 4 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 1000;
                            }
                            else{
                                sum += 1000;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 4){
                            if(token == 'R'){
                                sum += 1000;
                            }
                            else{
                                sum -= 1000;
                            }
                        }
                    }
                }

                whiteCount = 0;
                redCount = 0;

                /**
                 * Diagonal check
                 */

                if(k == 3){

                    int startRow = 0, startCol = 0;
                    int i, j;

                    if(k >= l){
                        if(l < 3){
                            startRow = 0;
                            startCol = k - l;
                        }
                        else{
                            startRow = l - 3;
                            startCol = k - 3;
                        }
                    }
                    else{
                        startRow = l - 3;
                        startCol = k - 3;
                    }

                    for ( ; startRow < 3 && startCol < 4 ; startRow++, startCol++) {
                        whiteCount = 0;
                        redCount = 0;
                        for (i = startRow, j = startCol; i < startRow + 4 && j < startCol + 4; i++, j++) {
                            if(this.grid[i][j] == 'R') {
                                redCount++;
                            } else if(this.grid[i][j] == 'W'){
                                whiteCount++;
                            }
                        }
                        if(whiteCount == 3 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 20;
                            }
                            else{
                                sum += 20;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 3){
                            if(token == 'R'){
                                sum += 20;
                            }
                            else{
                                sum -= 20;
                            }
                        }
                        else if(whiteCount == 2 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 10;
                            }
                            else{
                                sum += 10;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 2){
                            if(token == 'R'){
                                sum += 10;
                            }
                            else{
                                sum -= 10;
                            }
                        }
                        else if(whiteCount == 4 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 1000;
                            }
                            else{
                                sum += 1000;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 4){
                            if(token == 'R'){
                                sum += 1000;
                            }
                            else{
                                sum -= 1000;
                            }
                        }
                    }

                    if(k >= (5 - l)){
                        if(l > 2){
                            startRow = 5;
                            startCol = k - (5 - l);
                        }
                        else{
                            startRow = l + 3;
                            startCol = k - 3;
                        }
                    }
                    else{
                        if(k < 3){
                            startRow = l + k;
                            startCol = 0;
                        }
                        else{
                            startRow = l + 3;
                            startCol = k - 3;
                        }
                    }

                    for ( ; startRow > 2 && startCol < 4 ; startRow--, startCol++) {
                        whiteCount = 0;
                        redCount = 0;
                        for (i = startRow, j = startCol; i > startRow - 4 && j < startCol + 4; i--, j++) {
                            if(this.grid[i][j] == 'R') {
                                redCount++;
                            } else if(this.grid[i][j] == 'W'){
                                whiteCount++;
                            }
                        }
                        if(whiteCount == 3 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 20;
                            }
                            else{
                                sum += 20;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 3){
                            if(token == 'R'){
                                sum += 20;
                            }
                            else{
                                sum -= 20;
                            }
                        }
                        else if(whiteCount == 2 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 10;
                            }
                            else{
                                sum += 10;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 2){
                            if(token == 'R'){
                                sum += 10;
                            }
                            else{
                                sum -= 10;
                            }
                        }
                        else if(whiteCount == 4 && redCount == 0) {
                            if(token == 'R'){
                                sum -= 1000;
                            }
                            else{
                                sum += 1000;
                            }
                        }
                        else if(whiteCount == 0 && redCount == 4){
                            if(token == 'R'){
                                sum += 1000;
                            }
                            else{
                                sum -= 1000;
                            }
                        }
                    }
                }
            }
        }
        return utility + sum;
    }

    public int testEval() {
        int utility = 138;
        int sum = 0;

        if(this.GoalTest()) {
            return -1000;
        }

        char token;

        if(this.player.charAt(0) == 'W') {
            token = 'R';
        } else {
            token = 'W';
        }

        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 7; j++) {
                if(this.grid[i][j] == token) {
                    sum += evaluationTable[i][j];
                } else if(this.grid[i][j] != 0) {
                    sum -= evaluationTable[i][j];
                }
            }
        }
        return utility + sum;
    }

    /**
     * Prints the board to stdout, useful for debugging.
     * @return String which represents the board of the current state.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(char[] row : grid) {
            for(char token : row) {
                char ch = '0';
                switch(Character.toLowerCase(token)) {
                    case 'w':
                        ch = 'w';
                        break;
                    case 'r':
                        ch = 'r';
                        break;
                }
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    /**
     * Compares two states by checking if the boards are the same.
     * @param obj, a state.
     * @return true/false depending on whether the states are the same.
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof State)) {
            return false;
        }
        State that = (State) obj;
        for(int i = 0; i < this.grid.length; i++) {
            for(int j = 0; j < this.grid[i].length; j++) {
                if(this.grid[i][j] != that.grid[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * A naive hashing function for the states, not used, so we didn't
     * give it any more thought.
     * @return hash value for the current state.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        char check;
        if(this.player.equals("WHITE")) {
            check = 'w';
        } else {
            check = 'r';
        }
        for(char[] row : grid) {
            for(char token : row) {
                if(token == check) {
                    hash++;
                }
            }
        }
        return hash * 6151;
    }

    /**
     * Modifies the current state by placing a token and updating the
     * lastRow/lastCol variables, and switching the player.
     * @param action, the move that should be performed.
     */
    public void DoMove(int action) {
        char token;

        if(this.player.charAt(0) == 'W') {
            token = 'R';
        } else {
            token = 'W';
        }

        int row = 0;
        for(int i = 0; i < 6; i++) {
            if(this.grid[i][action] != 0) {
                row++;
            } else {
                break;
            }
        }

        this.grid[row][action] = token;
        this.lastRow = row;
        this.lastCol = action;

        if(token == 'W') {
            this.player = "WHITE";
        } else {
            this.player = "RED";
        }
    }

    /**
     * Undo's the last move that was performed on this state.
     * Note that we do not have to modify the lastRow/lastCol variables,
     * since after we use this action, our search algorithm does not evaluate
     * this state or look for a goal state(which depend on lastRow/lastCol).
     * @param action the move(column) that should be undone.
     */
    public void UndoMove(int action) {
        int row = 0;
        for(int i = 0; i < 6; i++) {
            if(this.grid[i][action] != 0) {
                row++;
            } else {
                break;
            }
        }

        this.grid[--row][action] = 0;

        if(this.player.equals("WHITE")) {
            this.player = "RED";
        } else {
            this.player = "WHITE";
        }
    }
}
