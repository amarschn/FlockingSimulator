class VerletParticle extends VerletParticle2D
{
  float lifespan;
  Vec2D velocity;
  Vec2D acceleration;
  
  VerletParticle(Vec2D loc, Vec2D vel, Vec2D accel)
  {
    super(loc);
    velocity = vel;
    acceleration = accel;
    lifespan = 255;
  }
  
  void update()
  {
    lifespan -= 5;
    velocity.addSelf(acceleration);
    x += velocity.x;
    y += velocity.y;
  }
  
  void display()
  {
    update();
    fill(255, lifespan);
    noStroke();
    ellipse(x, y, 2, 2);
  }
}
