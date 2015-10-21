/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerbot;

import com.xcoders.ws.Card;
import com.xcoders.ws.Player;
import com.xcoders.ws.PokerException;
import com.xcoders.ws.PokerWS;
import com.xcoders.ws.PokerWSService;
import com.xcoders.ws.Table;
import java.util.List;
import java.util.Random;


/**
 *
 * @author ravindu
 */
public class PokerBot {

    private  PokerWS pws;
    private  Integer tableState = -1;
    private  Integer botMoney = 5000;
    
    public static void main(String[] args) {
        try {
            //start new bot
            new PokerBot();
        } catch (PokerException ex) {
            ex.printStackTrace();
        }
    }
    
    public PokerBot() throws PokerException {
        pws = new PokerWSService().getPokerWSPort();
        
        final Long playerId = pws.login("bot", "bot1");
        
        final int tableId = pws.joinGame(playerId);
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                //run the bot till game ends
                //time interval for server calls : 500 milliseconds
                while(tableState != Table.SHOWDOWN){
                    try {
                        playBot(playerId, tableId);
                                                
                        Thread.sleep(500);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    
                        
                    
                }
            }
        }).start();
        
    }
    
    private void playBot(Long playerId,Integer tableId) throws PokerException{
        Table table = pws.getTable(tableId);
        
        List<Player> players = table.getPlayers();
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                
                //check if its the bots turn
                if (player.getStatus() == Player.ACTIVE ) {
                    
                    // according to the round perform action
                    switch(table.getStatus()){
                        case Table.WAIT_SMALLBIND:
                            pws.placeSmallBind(tableId, playerId, calculateSmallBind());
                            break;
                        case Table.WAIT_BIGBIND:
                            pws.placeSmallBind(tableId, playerId, calculateBigBind(table.getMaxBet()));
                            break;                            
                        
                        case Table.PRE_FLOP:
                            placeBet(table,player);
                            break;
                            
                        case Table.POST_FLOP:
                            placeBet(table,player);
                            break;
                        case Table.POST_TURN:
                            discardCard(table, player);
                            break;
                            
                        case Table.RIVER:
                            placeBet(table,player);
                            break;
                            
                    }
                }
                break;
            }
        }
    }
    
    private Integer calculateSmallBind(){
        int bind = botMoney /4;
        botMoney = botMoney - bind;
        return botMoney /4;
    }
    
    private Integer calculateBigBind(Integer smallBind){
        int bind = smallBind + (botMoney - smallBind /4);
        if ( bind > smallBind){
            botMoney = botMoney - bind;
            return bind;
        }else{
            botMoney = botMoney - smallBind;
            return smallBind;
        }
        
    }
    
    private void placeBet(Table table,Player player) throws PokerException{
        int bet = table.getMaxBet() + (botMoney - table.getMaxBet()) / 4;
        if (table.getMaxBet() > bet) {
            pws.fold(table.getId(), player.getId());
        }else{
            pws.bet(table.getId(), player.getId(), bet);
        }
    }
    
    private void discardCard(Table table,Player player) throws PokerException{
        int x = new Random().nextInt(player.getCards().size());
        pws.discardCard(table.getId(), player.getId(), player.getCards().get(x));
    }
}
