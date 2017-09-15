package org.gusdb.fgputil.functional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.fgputil.functional.FunctionalInterfaces.BinaryFunction;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.FunctionalInterfaces.PredicateWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Reducer;
import org.gusdb.fgputil.functional.FunctionalInterfaces.ReducerWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.TrinaryFunction;

public class Functions {

  private Functions() {}

  /**
   * Returns a function that takes a parameter of the given type and returns a string
   * @param <T> type to convert to string
   */
  public static class ToStringFunction<T> implements Function<T,String> {
    @Override
    public String apply(T obj) {
      return obj.toString();
    }
  }

  /**
   * Returns a copy (a new HashMap) of the input map with entries trimmed out whose keys do not pass the
   * passed predicate
   * 
   * @param inputMap a map
   * @param keyPred a filter function on the keys of the map
   * @return a copy of the input map with non-passing entries removed
   */
  public static <S,T> Map<S,T> pickKeys(Map<S,T> inputMap, Predicate<S> keyPred) {
    return pickKeys(inputMap, keyPred, new LinkedHashMap<S,T>());
  }

  /**
   * Adds any entries in the input map whose keys pass the predicate to the target map.  Performs the same
   * basic operation as the two-argument version but allows the caller to specify the target map
   * implementation (e.g. LinkedHashMap), or a non-empty target map if desired.
   * 
   * @param inputMap a map of values
   * @param keyPred a filter function on the keys of the map
   * @param target a map into which the unfiltered elements should be placed
   * @return the target map
   */
  public static <S,T> Map<S,T> pickKeys(Map<S,T> inputMap, Predicate<S> keyPred, Map<S,T> target) {
    for (Entry<S,T> entry : inputMap.entrySet()) {
      if (keyPred.test(entry.getKey())) {
        target.put(entry.getKey(), entry.getValue());
      }
    }
    return target;
  }

  /**
   * Returns a copy (a new HashMap) of the input map with entries trimmed out whose values do not pass the
   * passed predicate
   * 
   * @param inputMap a map
   * @param valuePred a filter function on the values of the map
   * @return a copy of the input map with non-passing entries removed
   */
  public static <S,T> Map<S,T> pickValues(Map<S,T> inputMap, Predicate<T> valuePred) {
    return pickValues(inputMap, valuePred, new HashMap<S,T>());
  }

  /**
   * Adds any entries in the input map whose values pass the predicate to the target map.  Performs the same
   * basic operation as the two-argument version but allows the caller to specify the target map
   * implementation (e.g. LinkedHashMap), or a non-empty target map if desired.
   * 
   * @param inputMap a map of values
   * @param valuePred a filter function on the values of the map
   * @param target a map into which the unfiltered elements should be placed
   * @return the target map
   */
  public static <S,T> Map<S,T> pickValues(Map<S,T> inputMap, Predicate<T> valuePred, Map<S,T> target) {
    for (Entry<S,T> entry : inputMap.entrySet()) {
      if (valuePred.test(entry.getValue())) {
        target.put(entry.getKey(), entry.getValue());
      }
    }
    return target;
  }

  /**
   * Converts an iterable of keys into a map from the key to a value generated by the passed function
   * 
   * @param keys input keys
   * @param function value generator
   * @return map from passed keys to generated values
   */
  public static <S,T> Map<S,T> getMapFromKeys(Iterable<S> keys, Function<S,T> function) {
    Map<S,T> result = new LinkedHashMap<>();
    for (S key : keys) {
      result.put(key, function.apply(key));
    }
    return result;
  }

  /**
   * Converts an iterable of values into a map where the key is a value generated by the passed function
   * 
   * @param values input values
   * @param function key generator
   * @return map from generated keys to passed values
   */
  public static <S,T> Map<S,T> getMapFromValues(Iterable<T> values, Function<T,S> function) {
    Map<S,T> result = new LinkedHashMap<>();
    for (T value : values) {
      result.put(function.apply(value), value);
    }
    return result;
  }

  /**
   * Maps the given iterable elements to a List containing mapped elements.  The passed
   * function is executed on each input element; its outputs are placed in a new list and returned.
   * 
   * @param inputs an iterable of input elements
   * @param function a function to be performed on each element
   * @return List of function outputs
   */
  public static <S,T,R extends S> List<T> mapToList(Iterable<R> inputs, Function<S,T> function) {
    List<T> result = new ArrayList<>();
    for (R obj : inputs) {
      result.add(function.apply(obj));
    }
    return result;
  }

  /**
   * Maps the given iterable elements to a List containing mapped elements.  The passed
   * function is executed on each input element and its iteration index; its outputs are
   * placed in a new list and returned.
   * 
   * @param inputs an iterable of input elements
   * @param function a function to be performed on each element
   * @return List of function outputs
   */
  public static <S,T,R extends S> List<T> mapToListWithIndex(Iterable<R> inputs, BinaryFunction<S, Integer, T> function) {
    List<T> result = new ArrayList<>();
    int i = 0;
    for (R obj : inputs) {
      result.add(function.apply(obj, i++));
    }
    return result;
  }

  /**
   * Using the iterable collection and the predicate function passed in, filters those
   * items that satisfy the predicate into a new list
   * 
   * @param inputs - iterable collection to filter
   * @param predicate - predicate function supplying the filter test
   * @return - new list of filtered items
   */
  public static <T> List<T> filter(Iterable<T> inputs, Predicate<T> predicate) {
    List<T> result = new ArrayList<>();
    for(T obj : inputs) {
      if(predicate.test(obj)) {
        result.add(obj);
      }  
    }
    return result;
  }

  /**
   * Using the iterable collection and the predicate function passed in, removes from the
   * given collection, those items that do not satisfy the predicate.
   * 
   * @param inputs - iterable collection to be modified
   * @param predicate - predicate function supplying the filter test
   */
  public static <T> void filterInPlace(Iterable<T> inputs, Predicate<T> predicate) {
    Iterator<T> iterator = inputs.iterator();
    while(iterator.hasNext()) {
      if(!predicate.test(iterator.next())) {
        iterator.remove();
      }
    }
  }

  /**
   * Returns a function that takes a key and returns the value in the passed map for that key
   * 
   * @param map any map
   * @return functional facade over the map's get method
   */
  public static <S,T> Function<S,T> toMapFunction(final Map<S,T> map) {
    return new Function<S,T>() {
      @Override public T apply(S key) {
        return map.get(key);
      }
    };
  }

  /**
   * Performs a reduce operation on the passed collection using the passed reducer
   * 
   * @param inputs an iterable of input elements
   * @param reducer reducer function
   * @param initialValue initial value passed to the reducer's reduce method along with the first element
   * @return reduction of the collection
   */
  public static <S,T> T reduce(Iterable<S> inputs, Reducer<S,T> reducer, T initialValue) {
    for (S next : inputs) {
      initialValue = reducer.reduce(initialValue, next);
    }
    return initialValue;
  }

  /**
   * Performs a reduce operation on the passed collection using the passed reducer function- a trinary
   * function that takes the (0-based) iteration index as a third argument.
   * 
   * @param inputs an iterable of input elements
   * @param reducer reducer function (with index capture)
   * @param initialValue initial value passed to the reducer's reduce method along with the first element
   * @return reduction of the collection
   */
  public static <S,T> T reduceWithIndex(Iterable<S> inputs, TrinaryFunction<T,S,Integer,T> reducer, T initialValue) {
    int index = 0;
    for (S next : inputs) {
      initialValue = reducer.apply(initialValue, next, index++);
    }
    return initialValue;
  }

  /**
   * Transforms the values of a map to new values linked to the same keys.  A new LinkedHashMap containing
   * the new values is returned (i.e. iteration order is maintained).  The old map is unmodified.  The new
   * map will be the same size as the old one.
   * 
   * @param map a map from key to old value
   * @param transform a function to transform old values to new values
   * @return a new map containing the same keys pointing to new values
   */
  public static <R,S,T> Map<R,T> transformValues(Map<R,S> map, Function<S,T> transform) {
    Map<R,T> newMap = new LinkedHashMap<>(); // maintain iteration order of the incoming map
    for (Entry<R,S> entry : map.entrySet()) {
      newMap.put(entry.getKey(), transform.apply(entry.getValue()));
    }
    return newMap;
  }

  /**
   * Takes a function that may or may not have checked exceptions and returns a new function that performs
   * the same operation but "swallows" any checked exception by wrapping it in a RuntimeException and
   * throwing that instead.  If calling code wishes to inspect the underlying exception it must catch the
   * RuntimeException and use getCause().
   * 
   * @param f function to wrap
   * @return a new function that swallows checked exceptions
   */
  public static <S,T> Function<S,T> fSwallow(FunctionWithException<S,T> f) {
    return x -> {
      try {
        return f.apply(x);
      }
      catch (Exception e) {
        throw (e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e));
      }
    };
  }

  /**
   * Takes a predicate that may or may not have checked exceptions and returns a new predicate that performs
   * the same operation but "swallows" any checked exception by wrapping it in a RuntimeException and
   * throwing that instead.  If calling code wishes to inspect the underlying exception it must catch the
   * RuntimeException and use getCause().
   * 
   * @param f predicate to wrap
   * @return a new predicate that swallows checked exceptions
   */
  public static <T> Predicate<T> pSwallow(PredicateWithException<T> f) {
    return x -> {
      try {
        return f.test(x);
      }
      catch (Exception e) {
        throw (e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e));
      }
    };
  }

  /**
   * Takes a reducer that may or may not have checked exceptions and returns a new reducer that performs
   * the same operation but "swallows" any checked exception by wrapping it in a RuntimeException and
   * throwing that instead.  If calling code wishes to inspect the underlying exception it must catch the
   * RuntimeException and use getCause().
   * 
   * @param r reducer to wrap
   * @return a new reducer that swallows checked exceptions
   */
  public static <S,T> Reducer<S,T> rSwallow(ReducerWithException<S,T> r) {
    return (accumulator, next) -> {
      try {
        return r.reduce(accumulator, next);
      }
      catch (Exception e) {
        throw (e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e));
      }
    };
  }
}
