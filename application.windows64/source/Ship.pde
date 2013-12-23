//********************************************************************************************
// Ship Class
class Ship
{ 
  // Body variables
  Body body;
  float boxWidth;
  float boxHeight;
  float radius;
  
  // movement variables
  //Vec2D position; not needed anymore with box2d
  Vec2 velocity;
  Vec2 acceleration;
  float thrust;
  Vec2 position;
  Vec2 pixelPosition;
  Vec2 force;
  Vec2 upForce;
  Vec2 downForce;
  Vec2 rightForce;
  Vec2 leftForce;
  Vec2 zeroForce;
  
  // Control signals for acceleration changes, forcefields, weapons, etc
  boolean left;
  boolean right;
  boolean up;
  boolean down;
  boolean forcePush;
  
  // Ship abilities
  float forcePushMagnitude;
  int fireDirection;
  Vec2 bulletVelocity;
  // Velocities for exhaust going in different directions, should I keep using VerletLib or move to box2d?
  Vec2D exhaustUp;
  Vec2D exhaustDown;
  Vec2D exhaustRight;
  Vec2D exhaustLeft;
  Vec2D randomExhaustAcceleration;
  
  //Vec2D adjustedPosition; // this allows the exhaust to be at the right position and won't get messed up by the ship's velocity
  ArrayList<VerletParticleSystem> verletParticles;
  ArrayList<Bullet> bulletList;
  // Colors of the ship
  int red;
  int green;
  int blue;
  

  float density;

  
  
  Ship(float x, float y)
  {
    // Basic characteristics
    boxWidth = 4;
    boxHeight = 4;
    density = 100;
    
    // Build Body
    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position.set(box2d.coordPixelsToWorld(x, y));
    body = box2d.createBody(bd);
    
    // Define a polygon shape
    PolygonShape ps = new PolygonShape();
    // Box2D considers the width and height of a rectangle to be the distance from the center to the edge
    //  (so half of what we normally think of as width or height)
    float box2dW = box2d.scalarPixelsToWorld(boxWidth/2);
    float box2dH = box2d.scalarPixelsToWorld(boxHeight/2);
    ps.setAsBox(box2dW, box2dH);
    
//    // Define a circle shape
//    CircleShape cs = new CircleShape();
//    radius = 8;
//    cs.m_radius = box2d.scalarPixelsToWorld(radius/2);
    
    // Define a fixture
    FixtureDef fd = new FixtureDef();
    fd.shape = ps;
    // Parameters that affect physics
    fd.density = density;
    fd.friction = 0.3;
    fd.restitution = 0.05;
    
    // Attach Fixture to Body
    body.createFixture(fd);
    body.setUserData(this);

    //position = new Vec2D(width/2, height/2);
    velocity = new Vec2(0, 0);
    position = body.getWorldCenter();
    acceleration = new Vec2(0, 0);
    thrust = 1000;
    force = new Vec2(0, 0);
    upForce = new Vec2(0 ,thrust);
    downForce = new Vec2(0, -thrust);
    rightForce = new Vec2(thrust, 0);
    leftForce = new Vec2(-thrust, 0);
    zeroForce = new Vec2(0, 0);
    
    
    
    
    exhaustUp = new Vec2D(0, 3);
    exhaustDown = new Vec2D(0, -3);
    exhaustRight = new Vec2D(-3, 0);
    exhaustLeft = new Vec2D(3, 0);
    
    verletParticles = new ArrayList<VerletParticleSystem>();
    bulletList = new ArrayList<Bullet>();
    
    
    red = 255;
    green = 255;
    blue = 255;
    
    forcePush = false;
    forcePushMagnitude = 100000;
    bulletVelocity = new Vec2(0, 0);
    
  }
  
  void upThrust()
  {
    this.up = true;
    this.fireDirection = 1;
  }
  void downThrust()
  {
    this.down = true;
    this.fireDirection = 2;
  }
  void leftThrust()
  {
    this.left = true;
    this.fireDirection = 3;
  }
  void rightThrust()
  {
    this.right = true;
    this.fireDirection = 4;
  }
  
  void zeroUpThrust()
  {
    this.up = false;
  }
  void zeroDownThrust()
  {
    this.down = false;
  }
  void zeroLeftThrust()
  {
    this.left = false;
  }
  void zeroRightThrust()
  {
    this.right = false;
  }

  void moveReset()
  {
    this.up = false;
    this.down = false;
    this.left = false;
    this.right = false;
  }
  
  void setForcePush(boolean value)
  {
    forcePush = value;
  }
  
  void move()
  { 
    force.addLocal((this.up == true) ? upForce : zeroForce);
    force.addLocal((this.down == true) ? downForce : zeroForce);
    force.addLocal((this.right == true) ? rightForce : zeroForce);
    force.addLocal((this.left == true) ? leftForce : zeroForce);
    body.applyForce(force, position);
    force.mulLocal(0);
  }
  
  // Start using behaviors
  Explosion explode()
  {
    return new Explosion(box2d.coordWorldToPixels(this.position));
  }
  
  void kill()
  {
    box2d.destroyBody(body);
  }
  
  void changeColor()
  {
    this.red -= 10;
    this.green += 10;
    this.blue += 10;
  }
  
  // Sends out a shockwave which accelerates enemies away from the Ship
  void forceField(EnemyList enemies)
  {
    // Send out particles in all directions
    //Vec2D adjustedP = new Vec2D(position.x + 5, position.y + 5);
    //verletParticles.add(new VerletParticleSystem(adjustedP, new Vec2D(0, 0), randomExhaustAcceleration));
    // initialize the force push outside of the for loop
    if (forcePush == true)
    {
      Vec2 forcePushVector;
      for (Enemy enemy : enemies.getList())
      {
        // Create force vector
        forcePushVector = position.sub(enemy.position);
        float d = forcePushVector.length();
        if (d < 15)
        {
          forcePushVector.mulLocal(-forcePushMagnitude/(d));
          //forcePushVector.limit(10);
          enemy.body.applyForce(forcePushVector, enemy.position);
        }
      }
    }
  }
  
  void fireBulletUp()
  {
    pixelPosition = box2d.coordWorldToPixels(this.position);
    bulletVelocity.set(this.body.getLinearVelocity().x, this.body.getLinearVelocity().y + 50);
    bulletList.add(new Bullet(this.pixelPosition.x, this.pixelPosition.y - 10, 2, 5, bulletVelocity)); //shoot up
  }
  
  void fireBulletDown()
  {
    pixelPosition = box2d.coordWorldToPixels(this.position);
    bulletVelocity.set(this.body.getLinearVelocity().x, this.body.getLinearVelocity().y - 50);
    bulletList.add(new Bullet(this.pixelPosition.x, this.pixelPosition.y + 10, 2, 5, bulletVelocity));
  }
  
  void fireBulletLeft()
  {
    pixelPosition = box2d.coordWorldToPixels(this.position);
    bulletVelocity.set(this.body.getLinearVelocity().x - 50, this.body.getLinearVelocity().y);
    bulletList.add(new Bullet(this.pixelPosition.x - 10, this.pixelPosition.y, 5, 2, bulletVelocity));
  }
  
  void fireBulletRight()
  {
    pixelPosition = box2d.coordWorldToPixels(this.position);
    bulletVelocity.set(this.body.getLinearVelocity().x + 50, this.body.getLinearVelocity().y);
    bulletList.add(new Bullet(this.pixelPosition.x + 10, this.pixelPosition.y, 5, 2, bulletVelocity));
  }
 
  
  void display()
  {
    for (VerletParticleSystem ps : verletParticles)
    {
      ps.update();
    }
    for (int i = bulletList.size()-1; i >= 0; i--)
    {
      Bullet b = bulletList.get(i);
      b.display();
      if (b.lifespan < 0 || b.markForDeletion == true)
      {
        bulletList.remove(b);
        b.killBody();
      }
    }
    // Get the body's position and angle
    Vec2 position = box2d.getBodyPixelCoord(body);
    float angle = body.getAngle();
    pushMatrix();
    translate(position.x, position.y);
    rotate(-angle);
    fill(this.red, this.green, this.blue); // Keep the spaceship the same color
    rectMode(CENTER);
    rect(0, 0, boxWidth, boxHeight);
    popMatrix();
    
  }
  
  void update()
  {
    display();
    move(); 
  }
}
