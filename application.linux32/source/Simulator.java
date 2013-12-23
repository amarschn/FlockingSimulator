import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Iterator; 
import toxi.physics2d.*; 
import toxi.physics2d.behaviors.*; 
import toxi.geom.*; 
import pbox2d.*; 
import org.jbox2d.common.*; 
import org.jbox2d.dynamics.joints.*; 
import org.jbox2d.collision.shapes.*; 
import org.jbox2d.collision.shapes.Shape; 
import org.jbox2d.common.*; 
import org.jbox2d.dynamics.*; 
import org.jbox2d.dynamics.contacts.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Simulator extends PApplet {

















/*
IDEAS

- better shape for ship and for enemies
- angle turning controls
  - I need an engine class and a gun class, and a way to apply forces to them
  - Need to be able to attach engines and weapons to body of ship
- Mothership
- Re-do everything
- Move over to Eclipse
- Try to implement interfaces and actual OO-design

- Event-driven explosions due to collisions
- Different levels of swarm tracking by enemies
- A mothership enemy that controls swarms of enemies, maybe like an observer pattern type thing
- Invisibility
- Decoys
- Teleportation
- Different backgrounds
- Homing missiles
- Lasers
- Other Weapons
- Fuel Consumption
  - some kind of fuel consumption logic for enemies, so that they avoid running out of fuel but use less thrusts
- Need to change the "applyForce" method so that it takes a group of forces as input and then outputs a vector
  with all of the forces averaged

Done ideas:
- Create a makeBody() function
- Make a better for-loop for all the enemies
- Asteroid avoidance => could be made better I think

Discarded ideas:
- Create a new abstract class => "Effect" and use it to make smoke clouds and explosions. Use Box2D => too processor intensive
*/

// Initialize the box2d instance variable
PBox2D box2d;

// Initialize all of the "sprites"
AsteroidSystem asteroids;
Ship Player;
EnemyList enemies;
ArrayList<Boundary> boundaryList = new ArrayList<Boundary>();
ArrayList<Explosion> explosionList = new ArrayList<Explosion>();

Vec2 mousePosition;

// Set up screen size
public void setup()
{
  size(1280, 800, P2D);
  noStroke();
  frameRate(60);
  // Initialize the physics engine
  box2d = new PBox2D(this);
  box2d.createWorld();
    
  // Listen for collisions
  box2d.listenForCollisions();
  
  // Set gravity
  box2d.setGravity(0, 0);

  
  // Initialize all of the sprites and sprite groups
  Player = new Ship(width/2, height/2);
  enemies = new EnemyList();
  asteroids = new AsteroidSystem();
  //Bugger = new SmartEnemy();
  
  mousePosition = new Vec2(mouseX, mouseY);
  
  // Add boundaries to the world, with arguments x location, y location, width, height
  boundaryList.add(new Boundary(width/2, -100, width + 200, 1)); // top of window
  boundaryList.add(new Boundary(-100, height/2, 1, height + 200)); // left of window
  boundaryList.add(new Boundary(width/2, height + 100, width + 200, 1)); // bottom of window
  boundaryList.add(new Boundary(width + 100, height/2, 1, height + 200)); // right of window
}

/*
***************************************************************************************************************
Game Loop
***************************************************************************************************************
*/
public void draw()
{
  // Update the physics world
  box2d.step();
  
  // Display background and information
  background(35);
  fill(255);
  text("Framerate: " + frameRate, width - 400, 10);
  text("Enemy Count: " + enemies.getCount(), width - 200, 10);
  
  // Update all the sprites
  Player.update();
  enemies.update(Player.position, asteroids);
  asteroids.update();
  //Bugger.update(Player.position, asteroids);
  
  mousePosition = box2d.coordPixelsToWorld(mouseX, mouseY);
  for (Boundary b : boundaryList)
  {
    b.display();
  }
  Iterator<Explosion> iterator = explosionList.iterator();
  while (iterator.hasNext())
  {
    Explosion e = iterator.next();
    if (e.isDead())
    {
      iterator.remove();
    }
    e.display();
  }
}

/*
***************************************************************************************************************
Collision Listener
***************************************************************************************************************
*/
public void beginContact(Contact cp)
{
  // Get both shapes
  Fixture f1 = cp.getFixtureA();
  Fixture f2 = cp.getFixtureB();
  
  // Get both bodies
  Body b1 = f1.getBody();
  Body b2 = f2.getBody();
  
  // Get objects that reference these bodies
  Object o1 = b1.getUserData();
  Object o2 = b2.getUserData();
  
  if (o1.getClass() == Asteroid.class && o2.getClass() == Enemy.class)
  {
    Enemy enemy = (Enemy) o2;
    enemy.delete();
    explosionList.add(enemy.explode());
    explosionList.add(enemy.explode());
    explosionList.add(enemy.explode());
  }
  else if(o1.getClass() == Enemy.class && o2.getClass() == Asteroid.class)
  {
    Enemy enemy = (Enemy) o1;
    enemy.delete();
    explosionList.add(enemy.explode());
  }
  else if(o1.getClass() == Enemy.class && o2.getClass() == Bullet.class)
  {
    Enemy enemy = (Enemy) o1;
    enemy.delete();
    explosionList.add(enemy.explode());
  }
  else if(o1.getClass() == Bullet.class && o2.getClass() == Enemy.class)
  {
    Enemy enemy = (Enemy) o2;
    enemy.delete();
    explosionList.add(enemy.explode());
  }

}
public void endContact(Contact cp)
{
}


/*************************************************************************************************************
 * User Input
**************************************************************************************************************/
public void keyPressed()
{
  // If key is one of the arrow keys, then the ship's accelerates in that direction
  if (key == CODED)
  {
    switch (keyCode)
    {
      case UP: Player.upThrust(); break;
      case DOWN: Player.downThrust(); break;
      case LEFT: Player.leftThrust(); break;
      case RIGHT: Player.rightThrust(); break;
    }
  }
  else
  {
    switch(key)
    {
      case 'e': enemies.addEnemy(); break;
      case 'k': enemies.clearList(); break;
      //case 'q': obstacles.addObstacle(); break;
      case 'q': asteroids.addAsteroid(); break;
      case 'p': Player.upThrust(); Player.downThrust(); Player.rightThrust(); Player.leftThrust();
      case ' ': Player.setForcePush(true); Player.forceField(enemies); break; // need to make a "true" value for this, as it doesn't respond super well
      case 'w': Player.fireBulletUp(); break;
      case 'a': Player.fireBulletLeft(); break;
      case 's': Player.fireBulletDown(); break;
      case 'd': Player.fireBulletRight(); break;
    }
  }
}

public void keyReleased()
{
  // If key is one of the arrow keys, then the ship's accelerates in that direction
  if (key == CODED)
  {
    switch (keyCode)
    {
      case UP: Player.zeroUpThrust(); break;
      case DOWN: Player.zeroDownThrust(); break;
      case LEFT: Player.zeroLeftThrust(); break;
      case RIGHT: Player.zeroRightThrust(); break;
    }
  }
  else
  {
    switch(key)
    {
      case ' ': Player.setForcePush(false); break;
    }
  }
}
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
    fd.friction = 0.3f;
    fd.restitution = 0.5f;
    fd.density = random(500, 5000);
    body.createFixture(fd);
    body.setUserData(this);

    // Set the color, make it mapped to the density of the asteroid
    shade = map(fd.density, 500, 5000, 100, 255);
  }
  
  public void display()
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
  
  public void killBody()
  {
    box2d.destroyBody(body);
  }
  
  public void update()
  {
    display();
  }
}
//****************************************************************************************************************
// Groups of asteroids
//*****************************************************************************************************************
class AsteroidSystem
{
  ArrayList<Asteroid> asteroids;
  
  AsteroidSystem()
  {
    asteroids = new ArrayList<Asteroid>();
  }
  
  public ArrayList<Asteroid> getList()
  {
    return asteroids;
  }
  
  public void addAsteroid()
  {
    asteroids.add(new Asteroid());
  }
  
  public void update()
  {
    for (Asteroid asteroid : asteroids)
    {
      asteroid.update();
    }
  }
}
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
    fd.friction = 0.3f;
    fd.restitution = 0.3f;
    //fd.density = 1.0; // density is unneeded, as this is a permanently fixed object
    body.createFixture(fd);
    body.setUserData(this);
  }
  
  public void display()
  {
    fill(255);
    noStroke();
    rectMode(CENTER);
    rect(x, y, w, h);
  }
}


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
  
  public void changeColor()
  {
    this.col = (255);
  }
  
  public void display()
  {
    super.display();
    // something is wrong with the lifespan/killBody() thing, so figure that out sometime
    lifespan -= 3;
  }
}


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
  
  
  
  public void display() {
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
  
  public void makeBody(float x, float y, float r) {
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
    fd.density = 0.1f;
    fd.friction = 0.1f;
    fd.restitution = 0.2f;

    // Attach fixture to body
    body.createFixture(fd);

    body.setUserData(this);
  }
}

class Enemy extends Ship
{
  float maxspeed;
  float maxforce;
  float steeringBias;
  float desiredSeparation;
  float cohesionRadius;
  float alignmentRadius;
  float repulsionRadius;
  boolean markForDeletion;
  
  // Experimental
  Vec2 seekForce;
  Vec2 separateForce;
  Vec2 alignForce;
  Vec2 cohesionForce;
  
  
  Enemy()
  {
    super(random(0, width), random(0, height));
    boxHeight = 4;
    boxWidth = 4;
    this.red = 255;
    this.green = 100;
    this.blue = 100;
    maxspeed = 35;
    maxforce = 0.1f;
    steeringBias = 0.1f; // determines how much steering magnitude is necessary to apply a thrust
    desiredSeparation = 3;
    cohesionRadius = 2;
    alignmentRadius = 3;
    repulsionRadius = 10;
    markForDeletion = false;
    
    // experimental
    seekForce = new Vec2(0, 0);
    separateForce = new Vec2(0, 0);
    alignForce = new Vec2(0, 0);
    cohesionForce = new Vec2(0, 0);
    
  }
  
  public void delete()
  {
    markForDeletion = true;
  }
  
  public void killBody()
  {
    box2d.destroyBody(body);
  }
  
  public void changeColor()
  {
    this.blue += 100;
  }
  
  public void applyThrust(Vec2 force)
  { 
    acceleration.y += (force.y < -steeringBias) ? -thrust : 0; // fire down thruster
    acceleration.y += (force.y > steeringBias) ? thrust : 0; // fire up thruster
    acceleration.x += (force.x > steeringBias) ? thrust : 0; // fire right thruster
    acceleration.x += (force.x < -steeringBias) ? -thrust : 0; // fire left thruster 
  }
  
  public void move()
  {
    body.applyForce(acceleration, position);
    acceleration.mulLocal(0); // zero out the acceleration every move cycle so it doesn't get to crazy numbers
  }
  
  // Create forces that will be compared and applied by the thrusters
  public void forceCreator(Vec2 target, ArrayList<Enemy> enemyList)
  {
    
    // seek
    seekForce = target.sub(position); // vector pointing from location to target
    float targetDistance = seekForce.length(); // distance between target and self
    seekForce.normalize();
    if (targetDistance < 5) // if distance is below 20 pixels, map the desired vector
    {
      float m = map(targetDistance, 0, 20, 0, maxspeed);
      seekForce.mulLocal(m);
    }
    else
    {
      seekForce.mulLocal(maxspeed);
    }
    seekForce.subLocal(body.getLinearVelocity());
    
    // separate
    separateForce = new Vec2(0, 0); // average this sum of all vectors to get a direction of movement
    int separateCount = 0;
    
    // align
    alignForce = new Vec2(0, 0);
    int alignCount = 0;
    
    // cohesion
    cohesionForce = new Vec2(0, 0);
    int cohesionCount = 0;
    
    // The almighty for loop
    for (Enemy other : enemyList) // loop through every enemy, add the normalized vector to a sum, then average it to get an applied acceleration
    {
      float distance = position.sub(other.position).length(); // find distance between position of self and other vehicle
      
      if ((distance > 0) && (distance < desiredSeparation)) // is that distance closer than desired and to a vehicle other than oneself?
      {
        Vec2 difference = this.position.sub(other.position);
        difference.normalize();
        separateForce.addLocal(difference);
        separateCount++;
      }
      
      if ((distance < alignmentRadius) && (distance > 0))
      {
        alignForce.addLocal(other.body.getLinearVelocity());
        alignCount++;
      }
      
      if ((distance < cohesionRadius) && (distance > 0))
      {
        cohesionForce.addLocal(other.position);
        cohesionCount++;
      }
    }
    
    
    if (separateCount > 0)
    {
      //vectorSum.mulLocal(1/count); // this does something strange, and causes the enemies to occasionally stop moving
      separateForce.normalize();
      separateForce.mulLocal(maxspeed); // multiply by max speed
      separateForce.subLocal(body.getLinearVelocity()); // find the difference between the current velocity and the vectorSum, which is your desired speed
    }
    
    if (alignCount > 0)
    {
      alignForce.normalize();
      alignForce.mulLocal(maxspeed);
      alignForce.subLocal(body.getLinearVelocity());
    }
    
    if (cohesionCount > 0)
    {
      cohesionForce.normalize();
    }
  }
  
  
  
  
  public Vec2 seek(Vec2 target)
  {
    Vec2 desired = target.sub(position); // vector pointing from location to target
    float d = desired.length(); // distance between target and self
    desired.normalize();
    if (d < 5) // if distance is below 20 pixels, map the desired vector
    {
      float m = map(d, 0, 20, 0, maxspeed);
      desired.mulLocal(m);
    }
    else
    {
      desired.mulLocal(maxspeed);
    }
    desired.subLocal(body.getLinearVelocity());
    return desired;
  }
  
  public Vec2 separate(ArrayList<Enemy> enemyList)
  {
    Vec2 vectorSum = new Vec2(0, 0); // average this sum of all vectors to get a direction of movement
    int count = 0;
    for (Enemy other : enemyList) // loop through every enemy, add the normalized vector to a sum, then average it to get an applied acceleration
    {
      float distance = position.sub(other.position).length(); // find distance between position of self and other vehicle
      
      if ((distance > 0) && (distance < desiredSeparation)) // is that distance closer than desired and to a vehicle other than oneself?
      {
        Vec2 difference = this.position.sub(other.position);
        difference.normalize();
        vectorSum.addLocal(difference);
        count++;
      }
    }
    
    if (count > 0)
    {
      //vectorSum.mulLocal(1/count); // this does something strange, and causes the enemies to occasionally stop moving
      vectorSum.normalize();
      vectorSum.mulLocal(maxspeed); // multiply by max speed
      vectorSum.subLocal(body.getLinearVelocity()); // find the difference between the current velocity and the vectorSum, which is your desired speed
    }
    return vectorSum;
  }
  
  // Averages the velocities in of all enemies in the area of sphere of radius alignmentRadius
  //  and applies that as a steering force
  public Vec2 align(ArrayList<Enemy> enemyList)
  {
    Vec2 vectorSum = new Vec2(0, 0);
    int count = 0;
    
    for (Enemy other : enemyList)
    {
      float distance = position.sub(other.position).length();
      
      if ((distance < alignmentRadius) && (distance > 0))
      {
        vectorSum.addLocal(other.body.getLinearVelocity());
        count++;
      }
    }
    
    if (count > 0)
    {
      vectorSum.normalize();
      vectorSum.mulLocal(maxspeed);
      vectorSum.subLocal(body.getLinearVelocity());
    }
    
    return vectorSum;
  }
  // Cohesion
  // For the average location (i.e. center) of all nearby enemies, calculate steering vector towards that location
  public Vec2 cohesion(ArrayList<Enemy> enemyList)
  {
    Vec2 vectorSum = new Vec2(0, 0);
    int count = 0;
    
    for (Enemy other : enemyList)
    {
      float distance = position.sub(other.position).length();
      if ((distance < cohesionRadius) && (distance > 0))
      {
        vectorSum.addLocal(other.position);
        count++;
      }
    }
    
    if (count > 0)
    {
      vectorSum.normalize();
    }
    
    return vectorSum;
  }
  
  
  // Shittier avoid obstacle method, keep this around to use for poor AI
  public Vec2 avoid(AsteroidSystem asteroids)
  {
    ArrayList<Asteroid> asteroidList = asteroids.getList();
    Vec2 avoidForce = new Vec2(0, 0);
    int count = 0;
    
    for (Asteroid a : asteroidList)
    {
      float distance = position.sub(a.position).length();
      println(distance);
      if (distance < repulsionRadius) // have to avoid the whole object, so include the radius
      {
        count++;
        Vec2 difference = this.position.sub(a.position);
        difference.mulLocal(1/(distance * distance * distance));
        difference.normalize();
        avoidForce.addLocal(difference);
      }
    }
    
    if (count > 0)
    {
      avoidForce.normalize();
      avoidForce.mulLocal(maxspeed);
      avoidForce.subLocal(body.getLinearVelocity());
    }
    
    return avoidForce;
  }
  
  public void applyBehaviors(Vec2 target, ArrayList<Enemy> enemyList, AsteroidSystem asteroids)
  {
    // Scaling notes: need a value around 0.015 for the seekForce, and not sure for separateForce
    
    Vec2 seekForce = seek(target);
    Vec2 separateForce = separate(enemyList);
    Vec2 alignmentForce = align(enemyList);
    Vec2 cohesionForce = cohesion(enemyList);
//    Vec2 avoidForce = avoid(asteroids);
//    Vec2 betterAvoidForce = betterAvoid(asteroids);
    
    // Scaling
    seekForce.mulLocal(10);
    separateForce.mulLocal(1);
    alignmentForce.mulLocal(0.1f);
    cohesionForce.mulLocal(0.1f);
//    avoidForce.mulLocal(100);
//    betterAvoidForce.mulLocal(1);
    
    applyThrust(seekForce);
    applyThrust(separateForce); // separation, alignment, cohesion are 3 forces used for flocks
    applyThrust(alignmentForce);
    applyThrust(cohesionForce);
//    applyThrust(avoidForce);
//    applyThrust(betterAvoidForce);
  }
  
  
  public void applyBehaviors2(Vec2 target, ArrayList<Enemy> enemyList, AsteroidSystem asteroids)
  {
    forceCreator(target, enemyList);
    // Scaling
    seekForce.mulLocal(10);
    separateForce.mulLocal(1);
    alignForce.mulLocal(0.1f);
    cohesionForce.mulLocal(0.1f);
    
    applyThrust(seekForce);
    applyThrust(separateForce); // separation, alignment, cohesion are 3 forces used for flocks
    applyThrust(alignForce);
    applyThrust(cohesionForce);
    
  }
  
  public void externalForce(Vec2 force)
  {
    acceleration.addLocal(force);
  }
  
  public void update()
  {
    super.update();
  }
}
class EnemyList
{
  
  ArrayList<Enemy> enemyList;
  Vec2D target;
  
  EnemyList()
  {
    enemyList = new ArrayList<Enemy>();
  }
  
  public void setTarget(Vec2D t)
  {
    target = t;
  }
  
  public void addEnemy()
  {
    enemyList.add(new Enemy());
  }
  
  public void clearList()
  {
    for (Enemy e : enemyList)
    {
      e.delete();
    }
  }
  
  public ArrayList<Enemy> getList()
  {
    return enemyList;
  }
  
  public int getCount()
  {
    return enemyList.size();
  }
  
  public void update(Vec2 target, AsteroidSystem asteroids)
  {
    
    for (int i = enemyList.size() - 1; i >= 0; i--)
    {
      Enemy e = enemyList.get(i);
      e.update();
      e.applyBehaviors2(target, enemyList, asteroids);
      
      if (e.markForDeletion == true)
      {
        enemyList.remove(e);
        e.killBody();
      }
    }
  }
  
}
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
  
  public boolean isDead()
  {
    return (lifespan <= 0);
  }
  
  public void display()
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
    fd.friction = 0.3f;
    fd.restitution = 0.05f;
    
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
  
  public void upThrust()
  {
    this.up = true;
    this.fireDirection = 1;
  }
  public void downThrust()
  {
    this.down = true;
    this.fireDirection = 2;
  }
  public void leftThrust()
  {
    this.left = true;
    this.fireDirection = 3;
  }
  public void rightThrust()
  {
    this.right = true;
    this.fireDirection = 4;
  }
  
  public void zeroUpThrust()
  {
    this.up = false;
  }
  public void zeroDownThrust()
  {
    this.down = false;
  }
  public void zeroLeftThrust()
  {
    this.left = false;
  }
  public void zeroRightThrust()
  {
    this.right = false;
  }

  public void moveReset()
  {
    this.up = false;
    this.down = false;
    this.left = false;
    this.right = false;
  }
  
  public void setForcePush(boolean value)
  {
    forcePush = value;
  }
  
  public void move()
  { 
    force.addLocal((this.up == true) ? upForce : zeroForce);
    force.addLocal((this.down == true) ? downForce : zeroForce);
    force.addLocal((this.right == true) ? rightForce : zeroForce);
    force.addLocal((this.left == true) ? leftForce : zeroForce);
    body.applyForce(force, position);
    force.mulLocal(0);
  }
  
  // Start using behaviors
  public Explosion explode()
  {
    return new Explosion(box2d.coordWorldToPixels(this.position));
  }
  
  public void kill()
  {
    box2d.destroyBody(body);
  }
  
  public void changeColor()
  {
    this.red -= 10;
    this.green += 10;
    this.blue += 10;
  }
  
  // Sends out a shockwave which accelerates enemies away from the Ship
  public void forceField(EnemyList enemies)
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
  
  public void fireBulletUp()
  {
    pixelPosition = box2d.coordWorldToPixels(this.position);
    bulletVelocity.set(this.body.getLinearVelocity().x, this.body.getLinearVelocity().y + 50);
    bulletList.add(new Bullet(this.pixelPosition.x, this.pixelPosition.y - 10, 2, 5, bulletVelocity)); //shoot up
  }
  
  public void fireBulletDown()
  {
    pixelPosition = box2d.coordWorldToPixels(this.position);
    bulletVelocity.set(this.body.getLinearVelocity().x, this.body.getLinearVelocity().y - 50);
    bulletList.add(new Bullet(this.pixelPosition.x, this.pixelPosition.y + 10, 2, 5, bulletVelocity));
  }
  
  public void fireBulletLeft()
  {
    pixelPosition = box2d.coordWorldToPixels(this.position);
    bulletVelocity.set(this.body.getLinearVelocity().x - 50, this.body.getLinearVelocity().y);
    bulletList.add(new Bullet(this.pixelPosition.x - 10, this.pixelPosition.y, 5, 2, bulletVelocity));
  }
  
  public void fireBulletRight()
  {
    pixelPosition = box2d.coordWorldToPixels(this.position);
    bulletVelocity.set(this.body.getLinearVelocity().x + 50, this.body.getLinearVelocity().y);
    bulletList.add(new Bullet(this.pixelPosition.x + 10, this.pixelPosition.y, 5, 2, bulletVelocity));
  }
 
  
  public void display()
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
  
  public void update()
  {
    display();
    move(); 
  }
}


// A basic rectangular object

abstract class Thing 
{
  
  boolean markForDeletion;

  // We need to keep track of a Body and a radius
  Body body;
  float boxWidth;
  float boxHeight;

  int col;

  Thing(float x, float y)
  {
    
    markForDeletion = false;
//    // This function puts the particle in the Box2d world
//    makeBody(x, y, 20, 20);

//    col = color(127);
    
  }

  // This function removes the particle from the box2d world
  public void killBody()
  {
    box2d.destroyBody(body);
  }

  public void delete()
  {
    markForDeletion = true;
  }


  // 
  public void display()
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
  public void makeBody(float x, float y, float w_, float h_)
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
    fd.friction = 0.01f;
    fd.restitution = 0.1f;

    // Attach fixture to body
    body.createFixture(fd);
    
    body.setUserData(this);

  }
}
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
  
  public void update()
  {
    lifespan -= 5;
    velocity.addSelf(acceleration);
    x += velocity.x;
    y += velocity.y;
  }
  
  public void display()
  {
    update();
    fill(255, lifespan);
    noStroke();
    ellipse(x, y, 2, 2);
  }
}
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
  
  public void display()
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
  
  public void update()
  {
    display();
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "Simulator" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
