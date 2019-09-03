package org.emoflon.neo.example.sokoban;

import org.eclipse.xtend2.lib.StringConcatenation;

@SuppressWarnings("all")
public class ExpectedBoards {
  public static String singleSokoban() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][@][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String twoSokoban() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][@][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][@][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String singleBlock() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][$][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String singleBoulder() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][#][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String singleEndPos() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][.][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String validBoard() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][@][ ][ ][.][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][#][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][$][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String validBoardMoveBlock() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][@][$][ ][.][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][#][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String validBoardMoveBlockAfterMove() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][@][*][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][#][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String validBoardMoveBoulder() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][@][#][ ][.][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][$][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    _builder.append("[ ][ ][ ][ ][ ][ ][ ][ ]");
    _builder.newLine();
    return _builder.toString();
  }
  
  public static String validBoardFileImport() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[ ][ ][#][#][#][#][#]");
    _builder.newLine();
    _builder.append("[#][#][#][@][ ][.][#]");
    _builder.newLine();
    _builder.append("[#][ ][$][ ][#][.][#]");
    _builder.newLine();
    _builder.append("[#][ ][ ][$][$][ ][#]");
    _builder.newLine();
    _builder.append("[#][.][ ][ ][#][ ][#]");
    _builder.newLine();
    _builder.append("[#][ ][ ][ ][$][.][#]");
    _builder.newLine();
    _builder.append("[#][#][#][#][#][#][#]");
    _builder.newLine();
    return _builder.toString();
  }
}
