package bguspl.set.ex;

import bguspl.set.Env;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)
    public Integer [] [] grid;
    protected List<Integer>[] slotsToPlayers;
    protected List<Integer>[] playersToSlots;

    protected boolean [] [] playerPlacedToken ;
    //private final ReadWriteLock lockCards;
    //private final ReadWriteLock lockTokens;





    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        this.slotsToPlayers = new ArrayList[slotToCard.length];  //array of slots which contains array of players
        for(int i = 0; i< slotsToPlayers.length; i++){
            slotsToPlayers[i] = new ArrayList();
        }
        this.playersToSlots = new ArrayList[this.env.config.players];
        for(int i = 0; i< playersToSlots.length; i++){
            playersToSlots[i] = new ArrayList();
        }
        this.grid = new Integer[env.config.rows][env.config.columns];
        //this.lockCards = new ReentrantReadWriteLock();
        //this.lockTokens = new ReentrantReadWriteLock();

        this.playerPlacedToken= new boolean[env.config.players][env.config.columns*env.config.rows];

    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted().collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        //this.lockCards.readLock().lock();
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        //this.lockCards.readLock().unlock();
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     *
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(int card, int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        //this.lockCards.writeLock().lock();
        cardToSlot[card] = slot;
        slotToCard[slot] = card;
        env.ui.placeCard(card, slot);

        boolean keepLoop = true;
        for(int row = 0; row < this.grid.length && keepLoop; row++) {
            for (int col = 0; col < this.grid[0].length && keepLoop; col++) {
                if (this.grid[row][col] == null && slot == col + this.grid[0].length*row) {//Anni
                    this.grid[row][col] = card;
                    keepLoop = false;
                }

            }

        }
        //this.lockCards.writeLock().unlock();
    }

    /**
     * Removes a card from a grid slot on the table.
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        //this.lockCards.writeLock().lock();
        // System.out.println("slot " + slot);
        if (slotToCard[slot] != null){
            int card = slotToCard[slot];
            //   System.out.println("card " + card);
            for(int id = 0 ; id < playersToSlots.length; id++){
                removeToken(id, slot);
            }
            env.ui.removeTokens(slot);

            slotToCard[slot] = null;
            cardToSlot[card] = null;
            boolean keepLoop = true;
            for(int row = 0; row < this.grid.length && keepLoop; row++){
                for(int col = 0; col < this.grid[0].length && keepLoop; col++){
                    if( (this.grid[row][col]) != null && (this.grid[row][col] == card)){
                        this.grid[row][col] = null;
                        keepLoop = false;
                    }

                }

            }
            env.ui.removeCard(slot);
        }
        //this.lockCards.writeLock().unlock();

    }

    /**
     * Places a player token on a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public void placeToken(int player, int slot) {
        //this.lockTokens.writeLock().lock();
        if(slotToCard[slot] != null){
            slotsToPlayers[slot].add(player);
            playersToSlots[player].add(slot);
            //    System.out.println("playersToSlots: " +  playersToSlots[player]);
            //     System.out.println("slotsToPlayers: " + slotsToPlayers[slot]);
            env.ui.placeToken(player, slot);
        }
//        if(playerPlacedToken[player][slot] == false){
//            playerPlacedToken[player][slot] = true;
//            //slotsToPlayers[slot].add(player);
//            //playersToSlots[player].add(slot);
//           /// System.out.println("playersToSlots: " +  playersToSlots[player]);
//           // System.out.println("slotsToPlayers: " + slotsToPlayers[slot]);
//            env.ui.placeToken(player, slot);
//        }
        //this.lockTokens.writeLock().unlock();
    }

    /**
     * Removes a token of a player from a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return       - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        //this.lockTokens.writeLock().lock();

        if (slotsToPlayers[slot] != null && slotsToPlayers[slot].contains(player)) {
            //    System.out.println("remove from slotsToPlayers " + slot);
            slotsToPlayers[slot].remove(slotsToPlayers[slot].indexOf(player));
            if (playersToSlots[player] != null && playersToSlots[player].contains(slot)) {
                //       System.out.println("remove from playersToSlots " + slot);
                playersToSlots[player].remove(playersToSlots[player].indexOf(slot));

            }
            env.ui.removeToken(player, slot);
            //     System.out.println("remove from gui ");
            //this.lockTokens.writeLock().unlock();
            return true;
        }


        //this.lockTokens.writeLock().unlock();
        return false;
    }


    // getCard method
    public Integer getCard(int slot) {
        if (slotToCard[slot] == null) {
            return null;
        }

        int card = this.slotToCard[slot];

        return card;

    }

    public Integer getSlot(int card) {
        if (cardToSlot[card] == null) {
            return null;
        }

        int slot =  this.cardToSlot[card];

        return slot;
    }

    public void removeAllCardsFromTable(){
        //this.lockCards.writeLock().lock();
        for(int row = 0; row < this.grid.length; row++) {
            for (int col = 0; col < this.grid[0].length; col++) {
                if(this.grid[row][col]!=null){
                    int card = this.grid[row][col];
                    int slot = cardToSlot[card];
                    removeCard(slot);
                }

            }
        }
        //this.lockCards.writeLock().unlock();
    }

    public Integer[] getSlotToCard(){
        //this.lockCards.readLock().lock();
        Integer[] array = this.slotToCard;
        //this.lockCards.readLock().unlock();
        return array;
    }


}