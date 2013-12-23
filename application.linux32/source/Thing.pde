

// A basic rectangular object

abstract class Thing 
{
  
  boolean markForDeletion;

  // We need to keep track of a Body and a radius
  Body body;
  float boxWidth;
  float boxHeight;

  color col;

  Thing(float x, float y)
  {
    
    markForDeletion = false;
//    // This function puts the particle in the Box2d world
//    makeBody(x, y, 20, 20);

//    col = color(127);
    
  }

  // This function removes the particle from the box2d world
  void killBody()
  {
    box2d.destroyBody(body);
  }

  void delete()
  {
    markForDeletion = true;
  }


  // 
  void display()
  {
    // We look at each body and get its screen position
    Vec2 position = box2d.getBodyPixelCoord(body);
    // Get its angle of rotation
    float a = body.getAngle();
    pushMatrix();
    translate(position.x, position.y);
    rotate(a);
    fill(col);
    rect(0, 0, boxWidth, boxHeight);
    // Let's add a line so we can see the rotation
    popMatrix();
  }

  // Here's our function that adds the particle to the Box2D world
  void makeBody(float x, float y, float w_, float h_)
  {
    boxWidth = w_;
    boxHeight = h_;
    
    // Define a body
    BodyDef bd = new BodyDef();
    // Set the position of the bodydef, remember, this is different than the position variable for the object class
    bd.position = box2d.coordPixelsToWorld(x, y);
    bd.type = BodyType.DYNAMIC;
    bd.bullet = true;
    body = box2d.createBody(bd);

    // Make the body's shape a circle
    PolygonShape ps = new PolygonShape();
    float box2dW = box2d.scalarPixelsToWorld(boxWidth/2);
    float box2dH = box2d.scalarPixelsToWorld(boxHeight/2);
    ps.setAsBox(box2dW, box2dH);

    FixtureDef fd = new FixtureDef();
    fd.shape = ps;
    // Parameters that affect physics
    fd.density = 1000;
    fd.friction = 0.01;
    fd.restitution = 0.1;

    // Attach fixture to body
    body.createFixture(fd);
    
    body.setUserData(this);

  }
}
