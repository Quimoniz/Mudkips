package org.quimoniz.wigglytuff;

public interface EntityReadListener {
  /* Method being called once an entity has been completly read from disk 
   */
  public void entityFinished(Entity e);
}
