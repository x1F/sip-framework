package one.x1f.sip.foundation.core.declarative.utils;

import java.util.ArrayList;
import one.x1f.sip.foundation.core.declarative.model.ModelMapper;

public class DeclarativeHelperTestModels {

  public static class NoArgsConstructorMapper implements ModelMapper<Object, Object> {

    public NoArgsConstructorMapper(String test) {}

    @Override
    public Object mapToTargetModel(Object sourceModel) {
      return null;
    }
  }

  public static class ExceptionThrowingConstructorMapper implements ModelMapper<Object, Object> {

    public ExceptionThrowingConstructorMapper() throws IllegalAccessException {
      throw new IllegalAccessException("Exception message");
    }

    @Override
    public Object mapToTargetModel(Object sourceModel) {
      return null;
    }
  }

  public static class MultipleMethodsMapper implements ModelMapper<Integer, Integer> {

    @Override
    public Integer mapToTargetModel(Integer sourceModel) {
      return null;
    }

    @SuppressWarnings("java:S1172")
    public String mapToTargetModel(String sourceModel) {
      return null;
    }
  }

  public static class MyIntegerList extends ArrayList<Integer> {}

  public static class MyExtendedIntegerList extends MyIntegerList {}
}
