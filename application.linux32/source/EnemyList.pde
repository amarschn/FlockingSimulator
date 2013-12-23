class EnemyList
{
  
  ArrayList<Enemy> enemyList;
  Vec2D target;
  
  EnemyList()
  {
    enemyList = new ArrayList<Enemy>();
  }
  
  void setTarget(Vec2D t)
  {
    target = t;
  }
  
  void addEnemy()
  {
    enemyList.add(new Enemy());
  }
  
  void clearList()
  {
    for (Enemy e : enemyList)
    {
      e.delete();
    }
  }
  
  ArrayList<Enemy> getList()
  {
    return enemyList;
  }
  
  int getCount()
  {
    return enemyList.size();
  }
  
  void update(Vec2 target, AsteroidSystem asteroids)
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
