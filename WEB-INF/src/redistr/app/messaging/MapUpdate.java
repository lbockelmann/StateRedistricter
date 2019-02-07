package redistr.app.messaging;

import redistr.app.algorithm.Move;
import redistr.app.interfaces.JSONSerializable;
import redistr.util.IOUtil;

import java.util.List;

public class MapUpdate implements JSONSerializable {
  public boolean algorithmDone = false;
  public List<Move> moves;

  public MapUpdate(boolean algorithmDone, List<Move> moves) {
    this.algorithmDone = algorithmDone;
    this.moves = moves;
  }

  public boolean isAlgorithmDone() {
    return algorithmDone;
  }

  public void setAlgorithmDone(boolean state) {
    this.algorithmDone = state;
  }

  public List<Move> getMoves() {
    return moves;
  }

  public void setMoves(List<Move> moves) {
    this.moves = moves;
  }

  public String toJSONString() {
    String jsonStringFormat = "{\"moves\": %s, \"algorithmDone\": \"%b\"}";
    String movesJson = IOUtil.listToJSONString(moves);
    return String.format(jsonStringFormat, movesJson, algorithmDone);
  }
}
