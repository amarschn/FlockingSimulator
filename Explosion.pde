/* Need to make this a kind of behavior, but instead of extending the "Thing" class, maybe it should extend an
"Effects" abstract class? In other words there are different explosion behaviors for different "Things", and each of
those behaviors will instantiate a member of the "Effects" abstract class, which will be run in a separate for loop at runtime? Hmmm.
The difference between the "Thing" class and the "Effect" class would be that Things are in the Box2D world, whereas Effects are just shapes and stuff
Do normal shapes have worse performance though? Not sure..
*/
class Explosion
{
  float radius;
  float red;
  float green;
  float blue;
  Vec2 origin;
  float lifespan;
  
  Explosion(Vec2 origin_)
  {
    origin = origin_;
    radius = random(1, 6);
    red = random(200, 255);
    green = random(50, 150);
    blue = random(50, 150);
    lifespan = 50;
  }
  
  boolean isDead()
  {
    return (lifespan <= 0);
  }
  
  void display()
  {
    fill(red, green, blue);
    ellipse(origin.x + random(-10,10), origin.y + random(-10,10), radius, radius);
    red -= 4;
    green -= 2;
    blue -= 4;
    radius += random(-1, 3);
    lifespan -= random(1,4);
  }
}
