/*
 * A single asteroid, uses Jbox2D for realistic body physics
 */

class Asteroid
{
  
  BodyDef bd;
  Body body;
  PolygonShape ps;
  FixtureDef fd;
  
  Vec2[] vertices; // An array of vectors
  int numberOfVertices;
  float verticeAngle;
  float shade;
  
  
  Vec2 position;
  Vec2 velocity;
  float angle;
  
  Asteroid()
  {
    
    // Defining the body definition
    bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position.set(box2d.coordPixelsToWorld(random(0, width), random(0, height)));
    
    // Defining the body
    body = box2d.createBody(bd);
    
    // Defining the Shape and vertices
    ps = new PolygonShape();
    numberOfVertices = 6; // set the number of vertices
    float currentAngle = 0;
    verticeAngle = (2 * PI)/numberOfVertices; // Get the angle between each vertice
    vertices =  new Vec2[numberOfVertices]; // Get array to pass to the PolygonShape.set() command
    for(int i = 0; i < numberOfVertices; i++)
    {
      float x = random(40, 80) * cos(currentAngle);
      float y = random(40, 80) * sin(currentAngle);
      Vec2 v = new Vec2(x, y); // Cannot be a straight line, I think that's the issue. gonna have to go trig on this bitch
      vertices[i] = box2d.vectorPixelsToWorld(v);
      currentAngle += verticeAngle;
    }
    position = body.getWorldCenter();
    velocity = new Vec2(random(-2, 2), random(-2, 2));
    // Set a random starting velocity
    body.setLinearVelocity(velocity);
    ps.set(vertices, numberOfVertices);
    
    // Defining the fixture definition
    fd = new FixtureDef();
    fd.shape = ps;
    fd.friction = 0.3;
    fd.restitution = 0.5;
    fd.density = random(500, 5000);
    body.createFixture(fd);
    body.setUserData(this);

    // Set the color, make it mapped to the density of the asteroid
    shade = map(fd.density, 500, 5000, 100, 255);
  }
  
  void display()
  {
    Vec2 position = box2d.getBodyPixelCoord(body);
    angle = body.getAngle();
    pushMatrix();
    translate(position.x, position.y);
    rotate(-angle);
    fill(shade);
    noStroke();
    rectMode(CENTER);
    beginShape();
    for (int i = 0; i < numberOfVertices; i++)
    {
      Vec2 v = box2d.vectorWorldToPixels(ps.getVertex(i));
      vertex(v.x, v.y);
    }
    endShape(CLOSE);
    popMatrix();
  }
  
  void killBody()
  {
    box2d.destroyBody(body);
  }
  
  void update()
  {
    display();
  }
}
