

class Effect extends Thing
{
  
  Vec2 velocity;
  float radius;
  float lifespan;
  
  Effect(float x_, float y_, float r_)
  {
    super(x_, y_);
    radius = r_;
    lifespan = 255;
    this.makeBody(x_, y_, radius);
    velocity = new Vec2(random(-5, 5), random(-5, 5));
    body.setLinearVelocity(velocity);
  }
  
  
  
  void display() {
    // We look at each body and get its screen position
    Vec2 pos = box2d.getBodyPixelCoord(body);
    // Get its angle of rotation
    float a = body.getAngle();
    pushMatrix();
    translate(pos.x, pos.y);
    rotate(a);
    fill(lifespan);
    ellipse(0, 0, radius * 2, radius * 2);
    popMatrix();
    
    lifespan -= 3;
    if (lifespan < 0)
    {
      this.delete();
    }
  }
  
  void makeBody(float x, float y, float r) {
    // Define a body
    BodyDef bd = new BodyDef();
    // Set its position
    bd.position = box2d.coordPixelsToWorld(x, y);
    bd.type = BodyType.DYNAMIC;
    body = box2d.createBody(bd);

    // Make the body's shape a circle
    CircleShape cs = new CircleShape();
    cs.m_radius = box2d.scalarPixelsToWorld(r);

    FixtureDef fd = new FixtureDef();
    fd.shape = cs;
    // Parameters that affect physics
    fd.density = 0.1;
    fd.friction = 0.1;
    fd.restitution = 0.2;

    // Attach fixture to body
    body.createFixture(fd);

    body.setUserData(this);
  }
}
