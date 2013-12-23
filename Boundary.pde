/*
 * Use boundaries on world edge to prevent player from flying off and disappearing
 */


class Boundary
{
  float x,y;
  float w, h;
  float boxWidth2D;
  float boxHeight2D;
  
  Body body;
  FixtureDef fd;
  
  Boundary(float x_, float y_, float w_, float h_)
  {
    x = x_;
    y = y_;
    w = w_;
    h = h_;
    
    BodyDef bd = new BodyDef();
    bd.position.set(box2d.coordPixelsToWorld(x, y));
    bd.type = BodyType.STATIC;
    body = box2d.createBody(bd);
    
    boxWidth2D = box2d.scalarPixelsToWorld(w/2);
    boxHeight2D = box2d.scalarPixelsToWorld(h/2);
    PolygonShape ps = new PolygonShape();
    
    ps.setAsBox(boxWidth2D, boxHeight2D);
    
    // Defining the fixture definition
    fd = new FixtureDef();
    fd.shape = ps;
    fd.friction = 0.3;
    fd.restitution = 0.3;
    //fd.density = 1.0; // density is unneeded, as this is a permanently fixed object
    body.createFixture(fd);
    body.setUserData(this);
  }
  
  void display()
  {
    fill(255);
    noStroke();
    rectMode(CENTER);
    rect(x, y, w, h);
  }
}
