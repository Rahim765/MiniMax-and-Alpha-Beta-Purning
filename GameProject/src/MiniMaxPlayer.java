import jdk.jshell.execution.JdiExecutionControlProvider;

import javax.swing.plaf.nimbus.State;
import java.util.*;

public class MiniMaxPlayer extends Player{

    private double MAX_DEPTH = 2.0;
    private double INFINITY = 9999.0;

    public MiniMaxPlayer(String color, int x, int y, Board board) {
        super(color, x, y, board);
    }


    public String bfs(MiniMaxPlayer opponent){
        double self_distance = 0.0;
        double opponent_distance = 0.0;
        Set<MiniMaxPlayer> players = new HashSet<MiniMaxPlayer>();
        players.add(this);
        players.add(opponent);

        Set<Piece> destination = new HashSet<Piece>();
        for (MiniMaxPlayer player : players) {
            if (player.color.equals("white")) destination = this.board.get_white_goal_pieces();
            else destination = this.board.get_black_goal_pieces();

            Queue<Piece> queue = new LinkedList<Piece>();
            HashMap<Piece, Boolean> visited = new HashMap<Piece, Boolean>();
            HashMap<Piece, Double> distances = new HashMap<Piece, Double>();

            for (int y = 0; y < this.board.ROWS_NUM; y++) {
                for (int x = 0; x < this.board.COLS_NUM; x++) {
                    visited.put(this.board.boardMap[y][x], false);
                    distances.put(this.board.boardMap[y][x], this.INFINITY);
                }
            }

            String player_pos = player.get_position();
            int x = Integer.parseInt(player_pos.split(",")[0]);
            int y = Integer.parseInt(player_pos.split(",")[1]);
            Piece player_piece = this.board.get_piece(x , y);

            queue.add(player_piece);
            visited.put(player_piece, true);
            distances.put(player_piece, 0.0);

            while (queue.size() != 0){
                Piece piece = ((LinkedList<Piece>) queue).removeFirst();

                Set<Piece> piece_temp = new HashSet<Piece>();

                piece_temp = this.board.get_piece_neighbors(piece);
                for (Piece p : piece_temp) {
                    if (!visited.get(p)){
                        double t = distances.get(piece) + 1;
                        distances.put(p, t);
                        visited.put(p, true);
                        queue.add(p);
                    }
                }

                double min_distance = this.INFINITY;

                for (Piece p_key : distances.keySet()) {
                    if (destination.contains(p_key)){
                        if (distances.get(p_key) < min_distance){
                            min_distance = distances.get(p_key);
                        }
                    }
                }

                if (player == this) self_distance = min_distance;
                else opponent_distance = min_distance;
            }
        }

        return self_distance + "," + opponent_distance;
    }

    public double evaluate(MiniMaxPlayer opponent){
        String distances = this.bfs(opponent);
        double self_distance = Double.parseDouble(distances.split(",")[0]);
        double opponent_distance  = Double.parseDouble(distances.split(",")[1]);

        double total_score = (5 * opponent_distance - self_distance) * (
                1 + this.walls_count / 2.0
                );

        return total_score;
    }

    /*   هیوریستیک بهبود یافته

    public double evaluate(MiniMaxPlayer opponent){
        String distances = this.bfs(opponent);
        double self_distance = Double.parseDouble(distances.split(",")[0]);
        double opponent_distance  = Double.parseDouble(distances.split(",")[1]);


            return ((opponent_distance * ( 10 /  ( 1+ (double )this.walls_count ) ) ) + (1000/self_distance) ) ;

    }

     */


    public String get_best_action(MiniMaxPlayer opponent){
        double best_action_value = - (this.INFINITY);
        String best_action = "";
        Set<String> legal_move = new HashSet<String>();
        legal_move = this.get_legal_actions(opponent);
        for (String action : legal_move) {
            this.play(action, true);
            if (this.is_winner()){
                this.undo_last_action();
                return action;
            }

            double action_value = this.evaluate(opponent);
            if (action_value > best_action_value){
                best_action_value = action_value;
                best_action = action;
            }

            this.undo_last_action();
        }

        return best_action;
    }

    /* public String get_best_action(MiniMaxPlayer opponent){
        double best_action_value = - (this.INFINITY);
        String best_action = "";
        Set<String> legal_move = new HashSet<String>();
        legal_move = this.get_legal_actions(opponent);
        for (String action : legal_move) {
            this.play(action, true);
            if (this.is_winner()){
                this.undo_last_action();
                return action;
            }
            double action_value =  minimax(opponent, false , 0);     // this.evaluate(opponent);
            if (action_value >= best_action_value ){
                best_action_value = action_value;
                best_action = action;
            }

            this.undo_last_action();
        }

        System.out.println(best_action_value);
        return best_action;
    }

    public double minimax(MiniMaxPlayer opponent, boolean turn , int depth) {
        if (this.is_winner()){
            return 1000;
        }
        if (opponent.is_winner()){
            return -1000;
        }
        if (depth >= MAX_DEPTH ) {
                String distances = this.bfs(opponent);
                double self_distance = Double.parseDouble(distances.split(",")[0]);
                double opponent_distance = Double.parseDouble(distances.split(",")[1]);
                if (self_distance <= opponent_distance) {
                    return evaluate(opponent);
                }
                return evaluate(this);
        }
        if (turn){
            String best_action = this.get_best_action2(opponent);
            this.play(best_action , true);
            double score =  minimax(opponent , false , depth+1);
            this.undo_last_action();
            return score;
        }else{
            String best_action = opponent.get_best_action2(this);
            opponent.play(best_action , true);
            double score =  minimax(opponent , true , depth+1);
            opponent.undo_last_action();
            return score;
        }
    }

    public double alphabeta(MiniMaxPlayer opponent , int depth , double alpha , double beta , boolean turn){
        if (depth<=0){
            return evaluate(opponent);
        }
        if (turn){
            double best_action_value = - (this.INFINITY);
            Set<String> legal_move = new HashSet<String>();
            legal_move = this.get_legal_actions(opponent);
            for (String action : legal_move) {
                this.play(action, true);
                double action_value = alphabeta(opponent,depth-1,alpha,beta,false);
                if (action_value > best_action_value){
                    best_action_value = action_value;
                }
                if (best_action_value > beta) break;
                if (best_action_value > alpha){
                    alpha = best_action_value;
                }
                this.undo_last_action();
            }
            return best_action_value;
        }else{
            double best_action_value = (opponent.INFINITY);
            Set<String> legal_move = new HashSet<String>();
            legal_move = opponent.get_legal_actions(this);
            for (String action : legal_move) {
                opponent.play(action, true);
                double action_value = alphabeta(opponent,depth-1,alpha,beta,true);
                if (action_value < best_action_value){
                    best_action_value = action_value;
                }
                if (best_action_value < alpha) break;
                if (best_action_value < beta){
                    beta = best_action_value;
                }
                opponent.undo_last_action();
            }
            return best_action_value;
        }
    }

*/


}
