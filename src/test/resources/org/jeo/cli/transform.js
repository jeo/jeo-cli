function transform(cursor) {
  return cursor.map(function(f) {
    return f.put(f.geometry().getCentroid());
  });
}