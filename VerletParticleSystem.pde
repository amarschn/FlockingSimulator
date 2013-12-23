class VerletParticleSystem
{
  ArrayList<VerletParticle> particles;
  Vec2D location;
  Vec2D velocity;
  Vec2D acceleration;
  
  VerletParticleSystem(Vec2D loc, Vec2D vel, Vec2D accel)
  {
    location = loc;
    velocity = vel;
    acceleration = accel;
    particles = new ArrayList<VerletParticle>();

    particles.add(new VerletParticle(location, velocity, acceleration));
  }
  
  void display()
  {
    Iterator<VerletParticle> iterator = particles.iterator();
    while (iterator.hasNext())
    {
      VerletParticle p = iterator.next();
      if (p.lifespan <= 0)
      {
        iterator.remove();
      }
      p.display();
    }
  }
  
  void update()
  {
    display();
  }
}
