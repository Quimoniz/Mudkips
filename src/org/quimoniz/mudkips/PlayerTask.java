package org.quimoniz.mudkips;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class PlayerTask implements Delayed {
  private long timeInNanoseconds = 0;
  private MudkipsPlayer mPlayer;
  private Type taskType;
  public enum Type {
    REMOVAL(0), UNJAIL(1);
    private final int id;
    private Type(int id) {
      this.id = id;
    }
    public int getId() {
      return id;
    }
  }
  public PlayerTask(MudkipsPlayer mPlayer, Type taskType, long delay) {
    timeInNanoseconds = System.nanoTime() + delay;
    this.taskType = taskType;
    this.mPlayer = mPlayer;
  }
  @Override
  public int compareTo(Delayed arg0) {
    Long methodTime = new Long(arg0.getDelay(TimeUnit.NANOSECONDS));
    return new Long(this.getDelay(TimeUnit.NANOSECONDS)).compareTo(methodTime);
  }

  @Override
  public long getDelay(TimeUnit arg0) {
    return arg0.convert(timeInNanoseconds - System.nanoTime(), TimeUnit.NANOSECONDS);
  }
  public Type getType() {
    return taskType;
  }
  public String getName() {
    return mPlayer.getName();
  }
  public MudkipsPlayer getPlayer() {
    return mPlayer;
  }
  @Override public boolean equals(Object obj) {
    if(obj instanceof PlayerTask)
      if(getName().equals(((PlayerTask)obj).getName()) && ((PlayerTask)obj).getType() == getType())
        return true;
    return false;
  }
}
