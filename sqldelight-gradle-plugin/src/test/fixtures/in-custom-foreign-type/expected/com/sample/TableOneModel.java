package com.sample;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.squareup.sqldelight.RowMapper;
import com.squareup.sqldelight.SqlDelightStatement;
import com.squareup.sqldelight.internal.TableSet;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;

public interface TableOneModel {
  String TABLE_NAME = "table_one";

  String _ID = "_id";

  String CREATE_TABLE = ""
      + "CREATE TABLE table_one (\n"
      + "  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT\n"
      + ")";

  long _id();

  interface Select_with_typesModel<T1 extends TableOneModel, T2 extends TableTwoModel> {
    @NonNull
    T1 table_one();

    @NonNull
    T2 table_two();
  }

  interface Select_with_typesCreator<T1 extends TableOneModel, T2 extends TableTwoModel, T extends Select_with_typesModel<T1, T2>> {
    T create(@NonNull T1 table_one, @NonNull T2 table_two);
  }

  final class Select_with_typesMapper<T1 extends TableOneModel, T2 extends TableTwoModel, T extends Select_with_typesModel<T1, T2>> implements RowMapper<T> {
    private final Select_with_typesCreator<T1, T2, T> creator;

    private final Factory<T1> tableOneModelFactory;

    private final TableTwoModel.Factory<T2> tableTwoModelFactory;

    public Select_with_typesMapper(Select_with_typesCreator<T1, T2, T> creator,
        Factory<T1> tableOneModelFactory, TableTwoModel.Factory<T2> tableTwoModelFactory) {
      this.creator = creator;
      this.tableOneModelFactory = tableOneModelFactory;
      this.tableTwoModelFactory = tableTwoModelFactory;
    }

    @Override
    @NonNull
    public T map(@NonNull Cursor cursor) {
      return creator.create(
          tableOneModelFactory.creator.create(
              cursor.getLong(0)
          ),
          tableTwoModelFactory.creator.create(
              cursor.getLong(1),
              cursor.isNull(2) ? null : tableTwoModelFactory.typeAdapter.decode(cursor.getString(2))
          )
      );
    }
  }

  interface Creator<T extends TableOneModel> {
    T create(long _id);
  }

  final class Mapper<T extends TableOneModel> implements RowMapper<T> {
    private final Factory<T> tableOneModelFactory;

    public Mapper(Factory<T> tableOneModelFactory) {
      this.tableOneModelFactory = tableOneModelFactory;
    }

    @Override
    public T map(@NonNull Cursor cursor) {
      return tableOneModelFactory.creator.create(
          cursor.getLong(0)
      );
    }
  }

  final class Factory<T extends TableOneModel> {
    public final Creator<T> creator;

    public Factory(Creator<T> creator) {
      this.creator = creator;
    }

    public SqlDelightStatement select_with_types(TableTwoModel.Factory<? extends TableTwoModel> tableTwoModelFactory,
        @Nullable List[] types) {
      List<String> args = new ArrayList<String>();
      int currentIndex = 1;
      StringBuilder query = new StringBuilder();
      query.append("SELECT * FROM table_one, table_two\n"
              + "    WHERE type IN ");
      query.append('(');
      for (int i = 0; i < types.length; i++) {
        if (i != 0) query.append(", ");
        query.append('?').append(currentIndex++);
        args.add(tableTwoModelFactory.typeAdapter.encode(types[i]));
      }
      query.append(')');
      return new SqlDelightStatement(query.toString(), args.toArray(new String[args.size()]), new TableSet("table_one", "table_two"));
    }

    public <T2 extends TableTwoModel, R extends Select_with_typesModel<T, T2>> Select_with_typesMapper<T, T2, R> select_with_typesMapper(Select_with_typesCreator<T, T2, R> creator,
        TableTwoModel.Factory<T2> tableTwoModelFactory) {
      return new Select_with_typesMapper<T, T2, R>(creator, this, tableTwoModelFactory);
    }
  }
}
