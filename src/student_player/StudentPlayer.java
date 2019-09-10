package student_player;

import java.util.ArrayList;

import boardgame.Board;
import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

/** A player file submitted by a student. */
/**
 * @author AnssamGhezala 260720743
 * 
 *
 */
public class StudentPlayer extends PentagoPlayer {

	/**
	 * You must modify this constructor to return your student number. This is
	 * important, because this is what the code that runs the competition uses to
	 * associate you with your agent. The constructor should do nothing else.
	 */
	public StudentPlayer() {
		super("260720743");
	}

	// This is the Quadrant of the coordinate of the piece that I need to fill if I
	// am about to lose
	String loseType;

	// Not used, but if there is a possible Win (3 or more pieces aligned) winType
	// represents the Quadrant of that Win
	String winType;

	/**
	 * @author AnssamGhezala This node representes both a PentagoMove move and an
	 *         int score It represents a "Node" in my tree for the alpha-beta
	 *         pruning, A move is associated with a score (its heuristic)
	 *
	 */
	public class Node {

		public PentagoMove move;
		public int score;

		public void setMove(PentagoMove move) {
			this.move = move;
		}

		public PentagoMove getMove() {
			return this.move;
		}

		public void setScore(int score) {
			this.score = score;
		}

		public int getScore() {
			return this.score;
		}
	}

	/**
	 * This is the primary method that you need to implement. The ``boardState``
	 * object contains the current state of the game, which your agent must use to
	 * make decisions.
	 */
	public Move chooseMove(PentagoBoardState boardState) {
		loseType = "";
		// You probably will make separate functions in MyTools.
		// For example, maybe you'll need to load some pre-processed best opening
		// strategies...
		MyTools.getSomething();

		// Get the current player (me)
		int me = boardState.getTurnPlayer();

		// Node from alpha-beta pruning
		Node alphaBetaNode = null;
		alphaBetaNode = handleMove(boardState, -(Integer.MAX_VALUE), Integer.MAX_VALUE, true, me, 0);

		// Move from blocking
		PentagoMove newMove = null;
		PentagoCoord fillCord = fillCoord(boardState, me); // get the coordinate of the piece to fill to block opponent
															// if possible

		// Evaluate if it wiser to block the opponent or to continue alpha-beta pruning
		PentagoBoardState newBoard = (PentagoBoardState) boardState.clone();
		newBoard.processMove(alphaBetaNode.move);
		int scoreAlphaBeta = isAboutToLose(newBoard, me);

		PentagoBoardState newBoard2 = (PentagoBoardState) boardState.clone();
		int block = isAboutToLose(newBoard2, me);

		// if I am able to block the opponent AND I get a higher score by blocking them
		if (fillCord != null && block > scoreAlphaBeta) {

			// Get quadrant of the coordinate that I need to fill to block opponent
			Quadrant currentQuadrant = getQuadrant(fillCord);

			if ("V".equals(loseType)) {
				if (currentQuadrant == Quadrant.TL || currentQuadrant == Quadrant.BL) {
					newMove = new PentagoMove(fillCord.getX(), fillCord.getY(), Quadrant.TR, Quadrant.BR, me);

				} else {

					newMove = new PentagoMove(fillCord.getX(), fillCord.getY(), Quadrant.TL, Quadrant.BL, me);
				}
				return newMove;
			} else if ("H".equals(loseType)) {

				if (currentQuadrant == Quadrant.TL || currentQuadrant == Quadrant.TR) {
					newMove = new PentagoMove(fillCord.getX(), fillCord.getY(), Quadrant.BR, Quadrant.BL, me);

				} else {

					newMove = new PentagoMove(fillCord.getX(), fillCord.getY(), Quadrant.TR, Quadrant.TL, me);
				}
				return newMove;
			} else {
				newMove = new PentagoMove(fillCord.getX(), fillCord.getY(), Quadrant.TR, Quadrant.BL, me);

				return newMove;
			}
		} else {
			// if it's wiser for me to do alphabeta pruning, then I return the move of the
			// node from alphabeta pruning
			return alphaBetaNode.move;
		}

	}

	/**
	 * @param BoardState
	 *            board
	 * @param int
	 *            alpha
	 * @param int
	 *            beta
	 * @param int
	 *            maximizingPlayer
	 * @param int
	 *            me: current player
	 * @param int
	 *            depth: depth of the tree
	 * @return heuristic of a Node representing a move (Node.move) and its score
	 *         (Node.score)
	 */
	public Node handleMove(PentagoBoardState board, int alpha, int beta, boolean maximizingPlayer, int me, int depth) {
		int score = 0;
		int Best;
		int lose = 0;
		int win = 0;
		Node node = new Node();
		node.setMove(null);
		node.setScore(-1);

		Node maxNode = new Node();
		Node minNode = new Node();

		ArrayList<PentagoMove> moves = board.getAllLegalMoves();

		if (!maximizingPlayer) {
			Best = -(Integer.MAX_VALUE);
		} else {
			Best = (Integer.MAX_VALUE);
		}

		// base case, evaluate score when over
		if (board.getWinner() != Board.NOBODY) {
			lose = isAboutToLose(board, me);
			win = isAboutToLose(board, 1 - me);
			if (board.getWinner() == Board.DRAW) { // if there's a draw
				score = 0;
			} else if (board.getWinner() == me) { // if i m the winner
				score = score - depth - lose + win * 10000000;
			} else { // if the oponnent wins
				score = score - depth + lose - win * 100;
			}

			node.setScore(score);
			return node;
		}

		if (maximizingPlayer) {
			for (int i = 0; i < moves.size(); i++) {

				PentagoMove move = moves.get(i);

				PentagoBoardState newBoard = (PentagoBoardState) board.clone();
				newBoard.processMove(move);
				lose = isAboutToLose(newBoard, me);
				win = isAboutToLose(newBoard, 1 - me);
				int val = handleMove(newBoard, alpha, beta, false, me, depth++).score - lose * 100 + win * 1000;
				Best = Math.max(Best, val);
				alpha = Math.max(alpha, Best);
				maxNode.setMove(move);
				maxNode.setScore(Best);
				if (alpha >= beta) {
					break; // pruning
				}

			}

			return maxNode;

		} else {

			for (int i = 0; i < moves.size(); i++) {
				PentagoMove move = moves.get(i);
				PentagoBoardState newBoard = (PentagoBoardState) board.clone();
				newBoard.processMove(move);
				lose = isAboutToLose(newBoard, 1 - me);
				win = isAboutToLose(newBoard, me);
				int val = handleMove(newBoard, alpha, beta, false, me, depth++).score - lose * 100 + win * 1000;
				Best = Math.min(Best, val);
				beta = Math.min(beta, Best);
				minNode.setMove(move);
				minNode.setScore(Best);
				if (alpha >= beta) {
					break; // pruning
				}

			}
			return minNode;
		}

	}

	/**
	 * @param PentagoCoord
	 *            fillCord: some point
	 * @return Quadrant in which the PentagoCoord point is in
	 */
	public Quadrant getQuadrant(PentagoCoord fillCord) {

		Quadrant currentQuad = null;
		if (fillCord.getX() < 3 && fillCord.getY() < 3) {
			currentQuad = Quadrant.TL;
		} else if (fillCord.getX() >= 3 && fillCord.getY() >= 3) {
			currentQuad = Quadrant.BR;
		} else if (fillCord.getX() >= 3 && fillCord.getY() < 3) {
			currentQuad = Quadrant.BL;
		} else {
			currentQuad = Quadrant.TR;
		}

		return currentQuad;
	}

	// Checks if there is a possible win, not used
	public PentagoCoord checkWin(PentagoBoardState board, int me) {

		String myColor = "";
		String opponentColor = "";

		if (me == 0) {
			myColor = "WHITE";
		} else {
			myColor = "BLACK";
		}
		if ("WHITE".equals(myColor)) {
			opponentColor = "BLACK";

		} else {
			opponentColor = "WHITE";
		}

		// Check Horizontal Win
		// If 3 horizontals in 1 quadrant
		String array[] = new String[6];
		Quadrant currentQuadrant;
		PentagoCoord winCoord = null;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 2; j++) {
				array[j] = board.getPieceAt(i, j).name();
				PentagoCoord currentCoord = new PentagoCoord(i, j);

				// i have 4 horizontals
				if (array[j].equals(board.getPieceAt(i, j + 1).name())
						&& (board.getPieceAt(i, j + 1).name()).equals(board.getPieceAt(i, j + 2).name())
						&& (board.getPieceAt(i, j + 2).name()).equals(board.getPieceAt(i, j + 3).name())
						&& myColor.equals(array[j])) {
					winType = "H";
					// get currentQuadrant
					if ("EMPTY".equals(board.getPieceAt(i, j + 4).name())) {

						winCoord = new PentagoCoord(i, j + 4);
						return winCoord;
					} else if (j > 0 && "EMPTY".equals(board.getPieceAt(i, j - 1).name())) {
						winCoord = new PentagoCoord(i, j - 1);
						return winCoord;
					}
				}

				// i have 4 verticals
				if (array[j].equals(board.getPieceAt(i + 1, j).name())
						&& (board.getPieceAt(i + 1, j).name()).equals(board.getPieceAt(i + 2, j).name())
						&& (board.getPieceAt(i + 2, j).name()).equals(board.getPieceAt(i + 3, j).name())
						&& myColor.equals(array[j])) {
					winType = "V";
					// get currentQuadrant
					if ("EMPTY".equals(board.getPieceAt(i + 4, j).name())) {
						winCoord = new PentagoCoord(i, j);
						return winCoord;
					} else if (i > 0 && "EMPTY".equals(board.getPieceAt(i - 1, j).name())) {
						winCoord = new PentagoCoord(i - 1, j);
						return winCoord;
					}
				}

			}
		}
		return winCoord;
	}

	/**
	 * @param board:
	 *            current state of board
	 * @param me:
	 *            current player
	 * @return int: this in defines how bad we are losing (used for the heu
	 */
	public int isAboutToLose(PentagoBoardState board, int me) {

		String myColor = "";
		String opponentColor = "";

		if (me == 0) {
			myColor = "WHITE";
		} else {
			myColor = "BLACK";
		}
		if ("WHITE".equals(myColor)) {
			opponentColor = "BLACK";

		} else {
			opponentColor = "WHITE";
		}

		String array[] = new String[6];
		PentagoCoord currentCoord = null;
		for (int j = 0; j < 6; j++) {

			int omg = 0;
			for (int i = 0; i < 4; i++) {
				array[i] = board.getPieceAt(i, j).name();

				// If I have 3 vertical consecutives
				if (array[i].equals(board.getPieceAt(i + 1, j).name())
						|| (board.getPieceAt(i + 1, j).name()).equals(board.getPieceAt(i + 2, j).name())
								&& opponentColor.equals(array[i])) {
					loseType = "V";
					// 0 1 2
					switch (i) {
					case 0:

						if (myColor.equals(board.getPieceAt(i + 3, j).name())
								|| myColor.equals(board.getPieceAt(i + 4, j).name())
								|| myColor.equals(board.getPieceAt(i + 5, j).name())) {

						}
						if ("EMPTY".equals(board.getPieceAt(i + 2, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 2, j);
							currentCoord = fill;
							return (int) Math.pow(10, 2);
						} else if ("EMPTY".equals(board.getPieceAt(i + 3, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 3, j);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i + 4, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 4, j);
							currentCoord = fill;
							return (int) Math.pow(10, 4);
						} else if ("EMPTY".equals(board.getPieceAt(i + 5, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 5, j);
							currentCoord = fill;
							return (int) Math.pow(10, 5);
						}
						return (int) Math.pow(10, 3);
					case 1:
						if (myColor.equals(board.getPieceAt(i + 3, j).name())
								|| myColor.equals(board.getPieceAt(i + 4, j).name())
								|| myColor.equals(board.getPieceAt(i - 1, j).name())) {

						}

						if ("EMPTY".equals(board.getPieceAt(i + 2, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 2, j);
							currentCoord = fill;
							return (int) Math.pow(10, 2);
						} else if ("EMPTY".equals(board.getPieceAt(i + 3, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 3, j);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i - 1, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 1, j);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i + 4, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 4, j);
							currentCoord = fill;
							return (int) Math.pow(10, 4);
						}
						return 3;
					case 2:
						if (myColor.equals(board.getPieceAt(i + 3, j).name())
								|| myColor.equals(board.getPieceAt(i - 1, j).name())
								|| myColor.equals(board.getPieceAt(i - 2, j).name())) {

						}
						if ("EMPTY".equals(board.getPieceAt(i + 2, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 2, j);
							currentCoord = fill;
							return (int) Math.pow(10, 2);
						} else

						if ("EMPTY".equals(board.getPieceAt(i + 3, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 3, j);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i - 1, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 1, j);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i - 2, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 2, j);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						}
						return (int) Math.pow(10, 3);
					case 3:
						if (myColor.equals(board.getPieceAt(i - 1, j).name())
								|| myColor.equals(board.getPieceAt(i - 2, j).name())
								|| myColor.equals(board.getPieceAt(i - 3, j).name())) {

						}
						if ("EMPTY".equals(board.getPieceAt(i + 2, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 2, j);
							currentCoord = fill;
							return (int) Math.pow(10, 2);
						} else if ("EMPTY".equals(board.getPieceAt(i - 1, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 1, j);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i - 2, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 2, j);
							currentCoord = fill;
							return (int) Math.pow(10, 4);
						} else if ("EMPTY".equals(board.getPieceAt(i - 3, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 3, j);
							currentCoord = fill;
							return (int) Math.pow(10, 5);
						}
						return (int) Math.pow(10, 3);
					}
					omg++;
				}

			}

			if (omg > 0) {

				return 3;
			}
		}

		for (int i = 0; i < 6; i++) {
			int omg = 0;
			for (int j = 0; j < 4; j++) {
				array[j] = board.getPieceAt(i, j).name();
				// i have 3 horizontal wins
				if (array[j].equals(board.getPieceAt(i, j + 1).name())
						|| (board.getPieceAt(i, j + 1).name()).equals(board.getPieceAt(i, j + 2).name())
								&& opponentColor.equals(array[j])) {
					loseType = "H";

					switch (j) {
					case 0:
						if (myColor.equals(board.getPieceAt(i, j + 3).name())
								|| (myColor.equals(board.getPieceAt(i, j + 4).name())
										&& myColor.equals(board.getPieceAt(i, j + 5).name()))) {

						}
						if ("EMPTY".equals(board.getPieceAt(i, j + 2).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 2);
							currentCoord = fill;
							return (int) Math.pow(10, 2);
						} else

						if ("EMPTY".equals(board.getPieceAt(i, j + 3).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 3);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i, j + 4).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 4);
							currentCoord = fill;
							return (int) Math.pow(10, 4);
						} else if ("EMPTY".equals(board.getPieceAt(i, j + 5).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 5);
							currentCoord = fill;
							return (int) Math.pow(10, 5);
						}
						return (int) Math.pow(10, 3);
					// break;
					case 1:
						if ((myColor.equals(board.getPieceAt(i, j + 3).name())
								&& myColor.equals(board.getPieceAt(i, j + 4).name()))
								|| myColor.equals(board.getPieceAt(i, j - 1).name())) {

						}
						if ("EMPTY".equals(board.getPieceAt(i, j + 2).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 2);
							currentCoord = fill;
							return (int) Math.pow(10, 2);
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 1).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 1);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i, j + 3).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 3);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i, j + 4).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 4);
							currentCoord = fill;
							return (int) Math.pow(10, 4);
						}
						return (int) Math.pow(10, 3);
					case 2:
						if (myColor.equals(board.getPieceAt(i, j + 3).name())
								|| (myColor.equals(board.getPieceAt(i, j - 1).name())
										&& myColor.equals(board.getPieceAt(i, j - 2).name()))) {

						}
						if ("EMPTY".equals(board.getPieceAt(i, j + 2).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 2);
							currentCoord = fill;
							return (int) Math.pow(10, 2);
						} else if ("EMPTY".equals(board.getPieceAt(i, j + 3).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 3);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 1).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 1);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 2).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 2);
							currentCoord = fill;
							return (int) Math.pow(10, 4);
						}
						return (int) Math.pow(10, 3);
					case 3:
						if ("EMPTY".equals(board.getPieceAt(i, j + 2).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 2);
							currentCoord = fill;
							return 2;
						} else if (myColor.equals(board.getPieceAt(i, j - 1).name())
								|| (myColor.equals(board.getPieceAt(i, j - 2).name())
										&& myColor.equals(board.getPieceAt(i, j - 3).name()))) {

						}

						if ("EMPTY".equals(board.getPieceAt(i, j - 1).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 1);
							currentCoord = fill;
							return (int) Math.pow(10, 3);
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 2).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 2);
							currentCoord = fill;
							return (int) Math.pow(10, 4);
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 3).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 3);
							currentCoord = fill;
							return (int) Math.pow(10, 5);
						}
						return (int) Math.pow(10, 3);
					}
					omg++;

				}

			}

			if (omg > 0) {

				return 3;
			}
		}

		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				// i have a left-right diagonal win
				if (opponentColor.equals((board.getPieceAt(i, j).name()))
						&& (board.getPieceAt(i, j).name()).equals(board.getPieceAt(i + 1, j + 1).name())
						|| ((board.getPieceAt(i + 1, j + 1).name()).equals(board.getPieceAt(i + 2, j + 2).name()))) {

					loseType = "DL";
					// start of i switch
					switch (i) {
					case 3:
						if (j == 0) {
							return 3;
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							} else if ("EMPTY".equals(board.getPieceAt(i - 3, j - 3).name())) {
								PentagoCoord fill = new PentagoCoord(i - 3, j - 3);
								currentCoord = fill;
								return (int) Math.pow(10, 5);
							}

						}

						// return currentCoord;
						return 3;
					case 1:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
							} else if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							}
						}
						// return currentCoord;
						return (int) Math.pow(10, 3);
					case 2:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						}

						// return currentCoord;
						return 3;
					case 0:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							} else if ("EMPTY".equals(board.getPieceAt(i + 5, j + 5).name())) {
								PentagoCoord fill = new PentagoCoord(i + 5, j + 5);
								currentCoord = fill;
								return (int) Math.pow(10, 5);
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else {

								return (int) Math.pow(10, 3);
							}
						}

						return (int) Math.pow(10, 3);
					}
					// end of i switch
				}
			}
		}
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				// i have a left-right diagonal win
				if (opponentColor.equals((board.getPieceAt(i, j).name()))
						&& (board.getPieceAt(i, j).name()).equals(board.getPieceAt(i + 1, j + 1).name())
						|| ((board.getPieceAt(i + 1, j + 1).name()).equals(board.getPieceAt(i + 2, j + 2).name()))) {

					loseType = "DL";
					// start of i switch
					switch (i) {
					case 3:
						if (j == 0) {
							return 3;
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							} else if ("EMPTY".equals(board.getPieceAt(i - 3, j - 3).name())) {
								PentagoCoord fill = new PentagoCoord(i - 3, j - 3);
								currentCoord = fill;
								return (int) Math.pow(10, 5);
							}

						}

						// return currentCoord;
						return (int) Math.pow(10, 3);
					case 1:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
							} else if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							}
						}
						// return currentCoord;
						return (int) Math.pow(10, 3);
					case 2:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						}

						// return currentCoord;
						return (int) Math.pow(10, 3);
					case 0:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							} else if ("EMPTY".equals(board.getPieceAt(i + 5, j + 5).name())) {
								PentagoCoord fill = new PentagoCoord(i + 5, j + 5);
								currentCoord = fill;
								return (int) Math.pow(10, 5);
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return (int) Math.pow(10, 4);
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return (int) Math.pow(10, 3);
							} else {

								return (int) Math.pow(10, 3);
							}
						}
						// return currentCoord;

						return (int) Math.pow(10, 3);
					}
					// end of i switch
					// i have a left-right diagonal win
				}
			}
		}
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				// i have a left-right diagonal win
				if (opponentColor.equals((board.getPieceAt(i, j + 2).name()))
						&& (board.getPieceAt(i, j + 2).name()).equals(board.getPieceAt(i + 1, j + 1).name())
						|| ((board.getPieceAt(i + 1, j + 1).name()).equals(board.getPieceAt(i + 2, j).name()))) {
					return (int) Math.pow(10, 4);
				}
			}
		}

		return 3;
	}

	/**
	 * @param PentagoBoard
	 *            board: the current state of the board
	 * @param int
	 *            me: the current player
	 * @return PentagoCoord currentCoord: the coordinate of the point in the point
	 *         that needs to be filled by the player to avoid losing
	 * 
	 *         This method checks if the current player is "under threat": if there
	 *         are 3 pieces aligned horizontally/vertically or diagonally then this
	 *         method returns the coordinates of a point to block and cancel this
	 *         alignment if possible
	 */
	public PentagoCoord fillCoord(PentagoBoardState board, int me) {

		String myColor = "";
		String opponentColor = "";

		if (me == 0) {
			myColor = "WHITE";
		} else {
			myColor = "BLACK";
		}
		if ("WHITE".equals(myColor)) {
			opponentColor = "BLACK";

		} else {
			opponentColor = "WHITE";
		}

		String array[] = new String[6];
		PentagoCoord currentCoord = null;
		for (int j = 0; j < 6; j++) {

			int omg = 0;
			for (int i = 0; i < 4; i++) {
				array[i] = board.getPieceAt(i, j).name();

				// If I have 3 vertical consecutives
				if (array[i].equals(board.getPieceAt(i + 1, j).name())
						&& (board.getPieceAt(i + 1, j).name()).equals(board.getPieceAt(i + 2, j).name())
						&& opponentColor.equals(array[i])) {
					loseType = "V";
					switch (i) {
					case 0:
						if (myColor.equals(board.getPieceAt(i + 3, j).name())
								|| myColor.equals(board.getPieceAt(i + 4, j).name())
								|| myColor.equals(board.getPieceAt(i + 5, j).name())) {

						}
						if ("EMPTY".equals(board.getPieceAt(i + 3, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 3, j);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i + 4, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 4, j);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i + 5, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 5, j);
							currentCoord = fill;
							return currentCoord;
						}
						return currentCoord;
					case 1:
						if (myColor.equals(board.getPieceAt(i + 3, j).name())
								|| myColor.equals(board.getPieceAt(i + 4, j).name())
								|| myColor.equals(board.getPieceAt(i - 1, j).name())) {
						}

						if ("EMPTY".equals(board.getPieceAt(i + 3, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 3, j);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i + 4, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 4, j);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i - 1, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 1, j);
							currentCoord = fill;
							return currentCoord;
						}
						// return currentCoord;
						return currentCoord;
					case 2:
						if (myColor.equals(board.getPieceAt(i + 3, j).name())
								|| myColor.equals(board.getPieceAt(i - 1, j).name())
								|| myColor.equals(board.getPieceAt(i - 2, j).name())) {

						}

						if ("EMPTY".equals(board.getPieceAt(i + 3, j).name())) {
							PentagoCoord fill = new PentagoCoord(i + 3, j);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i - 1, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 1, j);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i - 2, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 2, j);
							currentCoord = fill;
							return currentCoord;
						}
						return currentCoord;
					case 3:
						if (myColor.equals(board.getPieceAt(i - 1, j).name())
								|| myColor.equals(board.getPieceAt(i - 2, j).name())
								|| myColor.equals(board.getPieceAt(i - 3, j).name())) {

						}
						if ("EMPTY".equals(board.getPieceAt(i - 1, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 1, j);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i - 2, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 2, j);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i - 3, j).name())) {
							PentagoCoord fill = new PentagoCoord(i - 3, j);
							currentCoord = fill;
							return currentCoord;
						}

						return currentCoord;
					}
					omg++;
				}

			}

			if (omg > 0) {
				// return currentCoord;
				return currentCoord;
			}
		}

		for (int i = 0; i < 6; i++) {
			int omg = 0;
			for (int j = 0; j < 4; j++) {
				array[j] = board.getPieceAt(i, j).name();
				// i have 3 horizontal wins
				if (array[j].equals(board.getPieceAt(i, j + 1).name())
						&& (board.getPieceAt(i, j + 1).name()).equals(board.getPieceAt(i, j + 2).name())
						&& opponentColor.equals(array[j])) {
					loseType = "H";

					// 0 1 2
					switch (j) {
					case 0:
						if (myColor.equals(board.getPieceAt(i, j + 3).name())
								|| (myColor.equals(board.getPieceAt(i, j + 4).name())
										&& myColor.equals(board.getPieceAt(i, j + 5).name()))) {

						}

						if ("EMPTY".equals(board.getPieceAt(i, j + 3).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 3);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i, j + 4).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 4);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i, j + 5).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 5);
							currentCoord = fill;
							return currentCoord;
						}
						return currentCoord;
					case 1:
						if (myColor.equals(board.getPieceAt(i, j + 3).name())
								|| myColor.equals(board.getPieceAt(i, j + 4).name())
								|| myColor.equals(board.getPieceAt(i, j - 1).name())) {

						}

						if ("EMPTY".equals(board.getPieceAt(i, j + 3).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 3);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i, j + 4).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 4);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 1).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 1);
							currentCoord = fill;
							return currentCoord;
						}
						return currentCoord;
					case 2:
						if (myColor.equals(board.getPieceAt(i, j + 3).name())
								|| myColor.equals(board.getPieceAt(i, j - 1).name())
								|| myColor.equals(board.getPieceAt(i, j - 2).name())) {
						}

						if ("EMPTY".equals(board.getPieceAt(i, j + 3).name())) {
							PentagoCoord fill = new PentagoCoord(i, j + 3);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 1).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 1);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 2).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 2);
							currentCoord = fill;
							return currentCoord;
						}
						return currentCoord;
					case 3:
						if (myColor.equals(board.getPieceAt(i, j - 1).name())
								|| myColor.equals(board.getPieceAt(i, j - 2).name())
								|| myColor.equals(board.getPieceAt(i, j - 3).name())) {

						}

						if ("EMPTY".equals(board.getPieceAt(i, j - 1).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 1);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 2).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 2);
							currentCoord = fill;
							return currentCoord;
						} else if ("EMPTY".equals(board.getPieceAt(i, j - 3).name())) {
							PentagoCoord fill = new PentagoCoord(i, j - 3);
							currentCoord = fill;
							return currentCoord;
						}
						return currentCoord;
					}
					omg++;

				}

			}

			if (omg > 0) {

				return currentCoord;
			}
		}

		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				// i have a left-right diagonal win
				if (opponentColor.equals((board.getPieceAt(i, j).name()))
						&& (board.getPieceAt(i, j).name()).equals(board.getPieceAt(i + 1, j + 1).name())
						&& ((board.getPieceAt(i, j).name()).equals(board.getPieceAt(i + 2, j + 2).name()))) {

					loseType = "DL";
					// start of i switch
					switch (i) {
					case 3:
						if (j == 0) {
							return currentCoord;
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i - 3, j - 3).name())) {
								PentagoCoord fill = new PentagoCoord(i - 3, j - 3);
								currentCoord = fill;
								return currentCoord;
							}

						}

						return currentCoord;
					case 1:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
							} else if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return currentCoord;
							}
						}
						return currentCoord;
					case 2:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 3) {
							if ("EMPTY".equals(board.getPieceAt(i - 1, j - 1).name())) {
								PentagoCoord fill = new PentagoCoord(i - 1, j - 1);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i - 2, j - 2).name())) {
								PentagoCoord fill = new PentagoCoord(i - 2, j - 2);
								currentCoord = fill;
								return currentCoord;
							}
						}

						return currentCoord;
					case 0:
						if (j == 0) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i + 5, j + 5).name())) {
								PentagoCoord fill = new PentagoCoord(i + 5, j + 5);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 1) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return currentCoord;
							} else if ("EMPTY".equals(board.getPieceAt(i + 4, j + 4).name())) {
								PentagoCoord fill = new PentagoCoord(i + 4, j + 4);
								currentCoord = fill;
								return currentCoord;
							}
						} else if (j == 2) {
							if ("EMPTY".equals(board.getPieceAt(i + 3, j + 3).name())) {
								PentagoCoord fill = new PentagoCoord(i + 3, j + 3);
								currentCoord = fill;
								return currentCoord;
							} else {

								return currentCoord;
							}
						}
						return currentCoord;
					}
					// end of i switch
				}
			}
		}
		return currentCoord;
	}
}
