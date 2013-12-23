
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
    maxforce = 0.1;
    steeringBias = 0.1; // determines how much steering magnitude is necessary to apply a thrust
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
  
  void delete()
  {
    markForDeletion = true;
  }
  
  void killBody()
  {
    box2d.destroyBody(body);
  }
  
  void changeColor()
  {
    this.blue += 100;
  }
  
  void applyThrust(Vec2 force)
  { 
    acceleration.y += (force.y < -steeringBias) ? -thrust : 0; // fire down thruster
    acceleration.y += (force.y > steeringBias) ? thrust : 0; // fire up thruster
    acceleration.x += (force.x > steeringBias) ? thrust : 0; // fire right thruster
    acceleration.x += (force.x < -steeringBias) ? -thrust : 0; // fire left thruster 
  }
  
  void move()
  {
    body.applyForce(acceleration, position);
    acceleration.mulLocal(0); // zero out the acceleration every move cycle so it doesn't get to crazy numbers
  }
  
  // Create forces that will be compared and applied by the thrusters
  void forceCreator(Vec2 target, ArrayList<Enemy> enemyList)
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
  
  
  
  
  Vec2 seek(Vec2 target)
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
  
  Vec2 separate(ArrayList<Enemy> enemyList)
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
  Vec2 align(ArrayList<Enemy> enemyList)
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
  Vec2 cohesion(ArrayList<Enemy> enemyList)
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
  Vec2 avoid(AsteroidSystem asteroids)
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
  
  void applyBehaviors(Vec2 target, ArrayList<Enemy> enemyList, AsteroidSystem asteroids)
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
    alignmentForce.mulLocal(0.1);
    cohesionForce.mulLocal(0.1);
//    avoidForce.mulLocal(100);
//    betterAvoidForce.mulLocal(1);
    
    applyThrust(seekForce);
    applyThrust(separateForce); // separation, alignment, cohesion are 3 forces used for flocks
    applyThrust(alignmentForce);
    applyThrust(cohesionForce);
//    applyThrust(avoidForce);
//    applyThrust(betterAvoidForce);
  }
  
  
  void applyBehaviors2(Vec2 target, ArrayList<Enemy> enemyList, AsteroidSystem asteroids)
  {
    forceCreator(target, enemyList);
    // Scaling
    seekForce.mulLocal(10);
    separateForce.mulLocal(1);
    alignForce.mulLocal(0.1);
    cohesionForce.mulLocal(0.1);
    
    applyThrust(seekForce);
    applyThrust(separateForce); // separation, alignment, cohesion are 3 forces used for flocks
    applyThrust(alignForce);
    applyThrust(cohesionForce);
    
  }
  
  void externalForce(Vec2 force)
  {
    acceleration.addLocal(force);
  }
  
  void update()
  {
    super.update();
  }
}
