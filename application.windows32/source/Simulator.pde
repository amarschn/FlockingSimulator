
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
void setup()
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
void draw()
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
void beginContact(Contact cp)
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
void endContact(Contact cp)
{
}


/*************************************************************************************************************
 * User Input
**************************************************************************************************************/
void keyPressed()
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

void keyReleased()
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
