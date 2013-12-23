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
  
  ArrayList<Asteroid> getList()
  {
    return asteroids;
  }
  
  void addAsteroid()
  {
    asteroids.add(new Asteroid());
  }
  
  void update()
  {
    for (Asteroid asteroid : asteroids)
    {
      asteroid.update();
    }
  }
}
