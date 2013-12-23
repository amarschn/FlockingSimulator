

class Bullet extends Thing
{
  Vec2 velocity;
  float muzzleVelocity;
  float lifespan;
  
  
  Bullet(float x_, float y_, float w_, float h_, Vec2 v_)
  {
    super(x_, y_);
    // Determine the size of the bullet
    boxWidth = w_;
    boxHeight = h_;
    // Add the muzzle velocity to the initial starting velocity
    
    makeBody(x_, y_, boxWidth, boxHeight);
    // marks this as a bullet for better collision detection
    this.body.setLinearVelocity(v_);
    lifespan = 100;
    col = color(0, 255, 0);
  }
  
  void changeColor()
  {
    this.col = (255);
  }
  
  void display()
  {
    super.display();
    // something is wrong with the lifespan/killBody() thing, so figure that out sometime
    lifespan -= 3;
  }
}
