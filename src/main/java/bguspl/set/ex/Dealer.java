package bguspl.set.ex;
import java.nio.channels.ScatteringByteChannel;
import java.util.*;

import bguspl.set.Env;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    protected long timeLeft;
    public Queue<Player> playersQueue;
    public Object isThereSet;




    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        this.terminate = false;
        this.reshuffleTime = System.currentTimeMillis();
        this.playersQueue = new LinkedList<>();
        this.isThereSet = new Object();
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {

        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        Thread [] threads = new Thread[players.length];
        int i = 0;
        for (Player player: players) {
            threads[i] = new Thread(player);
            threads[i].start();
            i=i+1;
        }

        while (!shouldFinish()) {
            placeCardsOnTable();
            timerLoop();
            updateTimerDisplay(true);
            removeAllCardsFromTable(); // all the cards are removed from the table
        }
        announceWinners();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        terminate();


    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        reshuffleTime = env.config.turnTimeoutMillis + System.currentTimeMillis();

        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            removeCardsFromTable();
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {//x
        this.terminate=true;
        for(int i = this.players.length - 1; i>=0 ; i--) {
            this.players[i].terminate();
            // terminate the player threads, and the dealer thread
        }
        Thread.currentThread().interrupt();
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0 ;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeAllCardsFromTable() {
        blockPlayersTokens();
        returnAllToDeck();
        table.removeAllCardsFromTable();
        removeCardsFromTable();
        for(Player player : players){
            player.setsQueue.clear();
        }
        unblockPlayersTokens();
    }

    public void returnAllToDeck(){
        Integer[] array =this.table.getSlotToCard();
        for(Integer card: array){
            if(card!=null){
                this.deck.add(card);
            }

        }

    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        blockPlayersTokens();

        shuffleDeck();
        if(env.util.findSets(deck, 1).size() == 0){
            terminate();
        }
        int slot = 0;
        for (int i = 0 ; i < this.table.grid.length; i++){
            for(int j = 0; j < this.table.grid[i].length; j++){
                if(!this.deck.isEmpty() && this.table.grid[i][j] == null){
                    int card = this.deck.remove(0);
                    this.table.grid[i][j] = card;
                    this.table. placeCard(card, slot);
                    slot++;
                }
            }
        }
        //     }
        unblockPlayersTokens();

    }

    private void placeSpecificCardOnTable(int row, int column){
        int slot = column + this.table.grid[0].length * row;
        if(!this.deck.isEmpty()){
            int card = this.deck.remove(0);
            this.table.placeCard(card, slot);
            env.ui.placeCard(card, slot);
        }

    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        synchronized (this.isThereSet){
            try{
                if(this.timeLeft >= this.env.config.turnTimeoutWarningMillis){
                    this.isThereSet.wait(1000);
                }
                else{
                    this.isThereSet.wait(10);
                }
                removeCardsFromTable();
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {

        if (!reset){
            if(reshuffleTime - System.currentTimeMillis() > env.config.turnTimeoutWarningMillis){
                timeLeft = reshuffleTime - System.currentTimeMillis();
                env.ui.setCountdown(Math.max(timeLeft, 0),false);
            }
            else{
                timeLeft = reshuffleTime - System.currentTimeMillis();
                env.ui.setCountdown(Math.max(timeLeft,0), true);
            }
        }
        else{
            reshuffleTime = env.config.turnTimeoutMillis + System.currentTimeMillis();
            timeLeft =reshuffleTime - System.currentTimeMillis();
            env.ui.setCountdown(Math.max(timeLeft, 0), false);
        }


    }
    /**
     * Returns all the cards from the table to the deck.
     *

     /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {

        int maxScore = 0;
        for (Player player : players) {
            if (player.score() > maxScore) {
                maxScore = player.score();
            }
        }

        int counter = 0;
        for (Player player : players) {
            if (player.score() == maxScore) {
                counter++;
            }
        }

        int [] winners = new int[counter];
        int numOfWinners = 0;
        for (Player player : players) {
            if (player.score() == maxScore) {
                winners[numOfWinners] = player.id;
                numOfWinners = numOfWinners + 1;
            }
        }
        env.ui.announceWinner(winners);

    }




    // The following methods are added for the purpose of the exercise.
    protected boolean testSet(int[] cards) {//we added it

        if(cards.length != 3) {
            return false;
        }
        boolean isSet = env.util.testSet(cards);
        if(isSet) {
            blockPlayersTokens();
            for (int i = 0; i < 3; i++) {
                int card = cards[i];
                int slot = this.table.cardToSlot[card];
                for (int row = 0; row < this.table.grid.length; row++) {
                    for (int col = 0; col < this.table.grid[0].length; col++) {
                        if (this.table.grid[row][col] == card) {
                            table.removeCard(slot);

                            if (!this.deck.isEmpty()) {
                                placeSpecificCardOnTable(row, col);
                            }

                        }

                    }
                }
            }
            unblockPlayersTokens();
            return true;
        }

        return false;

    }

    private void shuffleDeck(){
        Collections.shuffle(this.deck);
    }
    public void playerToQueue(Player player){

        this.playersQueue.add(player);
    }

    private void removeCardsFromTable() {
        try{
        while (!playersQueue.isEmpty()) {
            Player player = this.playersQueue.poll();
            if(player == null) {
                break;
            }
                List<Integer> tokens = this.table.playersToSlots[player.id];
                if (tokens.size() == 3) {
                    int[] set = new int[3];
                    for (int i = 0; i < 3; i++) {
                        set[i] = this.table.getCard(tokens.get(i));
                    }
                    if (testSet(set)) {
                        player.setPointToTrue();
                        updateTimerDisplay(true);
                    } else {
                        blockPlayersTokens();
                        List<Integer> tokensToRemove = this.table.playersToSlots[player.id];
                        int[] intArray = new int[tokensToRemove.size()];
                        for (int i = 0; i < tokensToRemove.size(); i++) {
                            intArray[i] = tokensToRemove.get(i);
                        }
                        tokensToRemove.clear();
                        for (int token : intArray) {
                            this.table.removeToken(player.id, token);
                        }
                        unblockPlayersTokens();
                        player.setPenaltyToTrue();
                    }
                }
            }
            }
        catch (ConcurrentModificationException e){
            removeCardsFromTable();
        }
    }

    private void blockPlayersTokens() {
        for (int i = 0; i < players.length; i++) {
            players[i].setLockTable(true);
        }
    }

    private void unblockPlayersTokens() {
        for (int i = 0; i < players.length; i++) {
            players[i].setLockTable(false);
        }
    }


}