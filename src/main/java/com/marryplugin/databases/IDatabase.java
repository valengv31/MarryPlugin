package com.marryplugin.databases;

import com.marryplugin.models.Marriage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IDatabase {
     void connect();
     void createTables();
     boolean save(Marriage marriage);
     boolean delete(Marriage marriage);
     List<String> getHistory(UUID playerId,int limit);
     Map<UUID, Marriage> loadFromDatabase();
     void close();
}
