/**
   Copyright Mob&Me 2012 (@MobAndMe)

   Licensed under the LGPL Lesser General Public License, Version 3.0 (the "License"),  
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.gnu.org/licenses/lgpl.html

   Unless required by applicable law or agreed to in writing, software 
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   Website: http://adaframework.com
   Contact: Txus Ballesteros <txus.ballesteros@mobandme.com>
*/

package com.mobandme.ada;

import java.util.ArrayList;
import java.util.List;

import com.mobandme.ada.exceptions.AdaFrameworkException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * This class encapsulate all logic of Database merging objects.
 * @version 2.4.3
 * @author Mob&Me
 */
class DatabaseMerger {
	class DatabaseMergeResult {
		static final int ACTION_ADD = 1;
		static final int ACTION_DELETE = 2;
		static final int ACTION_NOTHING = 3;
		
		String FieldName = "";
		DataMapping Mapping = null;
		int Action = ACTION_NOTHING;
	}
	
	/********************************
	 * 			PROPERTIES 			*
	 ********************************/
	private List<String> processedTables = new ArrayList<String>();
	
	private SQLiteDatabase databse = null;
	SQLiteDatabase getDatabse() { return databse; }
	private void setDatabse(SQLiteDatabase pDataBase) { this.databse = pDataBase; }
	
	
	
	
	/********************************
	 * 			CONSTRUCTORS		*
	 ********************************/
	/**
	 * Default class constructor.
	 * @param pDataBase Active database instance.
	 */
	public DatabaseMerger(SQLiteDatabase pDataBase) {
		setDatabse(pDataBase);
	}
	
	
	
	/********************************
	 * 			PUBLIC METHODS		*
	 ********************************/
	/**
	 * This method generate the Database table Script. 
	 * @param pObjectSet
	 * @return Database executable script.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public String[] getDatatableScript(ObjectSet<?> pObjectSet) throws AdaFrameworkException {
		String[] returnedValue = null;
		
		try {
			
			if (pObjectSet != null) {
				
				String tableName = pObjectSet.getDataBaseTableName();
				if (tableName != null && tableName.trim() != "") {
					
					processedTables.add(tableName);
					if (tableExists(tableName)) {
						String[] modelDefinedFields = getDefinedFields(pObjectSet);
						String[] databaseDefinedFields = getTableFields(tableName);
						
						//Merge the physical and virtual definitions.
						List<DatabaseMergeResult> mergeResult = mergeModels(modelDefinedFields, databaseDefinedFields);
						if (mergeResult != null && mergeResult.size() > 0) {
							returnedValue = generateAlterTable(tableName, mergeResult, pObjectSet);
						}
					} else {
						returnedValue = pObjectSet.getDataBaseTableScript();
					}
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(e);
		} 
		
		return returnedValue;
	}
	
	/**
	 * This method generate the database scripts to delete the tables does not in use.
	 * @return Array with the drop tables scripts.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public String[] getDatabaseCleanScripts() throws AdaFrameworkException {
		String[] returnedValue = null;
		List<String> scriptsList = new ArrayList<String>();
		
		try {
			
			String[] existingTables = getTables();
			if (existingTables != null && existingTables.length > 0) {
				if (processedTables != null && processedTables.size() > 0) {
					for(String databaseTable : existingTables) {
						boolean tableFound = false;
						for (String modelTable : processedTables) {
							if (databaseTable.trim().toLowerCase().equals(modelTable.trim().toLowerCase())) {
								tableFound = true;
								break;
							}
						}
						
						if (!tableFound) {
							if (!databaseTable.contains(DataUtils.DATABASE_LINKED_TABLE_NAME_PREFIX)) {
								scriptsList.add(String.format(DataUtils.DATABASE_DROP_TABLE_PATTERN, databaseTable));
							}
						}
					}
				}
			}
			
			if (scriptsList.size() > 0) {
				returnedValue = scriptsList.toArray(new String[scriptsList.size()]);
			}

		} catch (Exception e) {
			ExceptionsHelper.manageException(e);
		} finally {
			scriptsList.clear();
			scriptsList = null;
		}
		
		return returnedValue;
	}
	
	
	
	/********************************
	 * 			PRIVATE METHODS		*
	 ********************************/
	/**
	 * This method generate the alter table script.
	 * @param pTableName The table name.  
	 * @param pMergeResult The result of the merge process.
	 * @param pObjectSet Managed ObjectSet.
	 * @return SQLite Alter Table Script.
	 */
	private String[] generateAlterTable(String pTableName, List<DatabaseMergeResult> pMergeResult, ObjectSet<?> pObjectSet) {
		List<String> scriptsList = new  ArrayList<String>();
		String[] returnedValue = null;
		
		for(DatabaseMergeResult result : pMergeResult) {
			switch (result.Action) {
				case DatabaseMergeResult.ACTION_ADD:
					DataMapping mapping = getDataMapping(result.FieldName, pObjectSet);
					
					String fieldScript = generateFieldScript(mapping);
					if (fieldScript != null) {
						scriptsList.add(String.format("ALTER TABLE %s ADD COLUMN %s", pTableName, fieldScript)); 
					}
					break;
			}
		}
		
		if (scriptsList.size() > 0) {
			returnedValue = scriptsList.toArray(new String[scriptsList.size()]); 
		}
		scriptsList.clear();
		scriptsList = null;
		
		return returnedValue;
	}
	
	/**
	 * This method generate the database script part to define the field into the table.
	 * @param pMapping DataMapping information.
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String generateFieldScript(DataMapping pMapping) {
		String returnedValue = null;
		
		if (pMapping != null) {
			
			String dataFieldName = pMapping.DataBaseFieldName;
			String dataTypeScript = "";
			String dataPrimaryKeyScript = "";
			String dataAllowNullsScript = "";
			String dataDefaultValue = "";
			
			
			switch(pMapping.DataBaseDataType) {
				case Entity.DATATYPE_BOOLEAN:
				case Entity.DATATYPE_INTEGER:
				case Entity.DATATYPE_LONG:
				case Entity.DATATYPE_DOUBLE:
				case Entity.DATATYPE_ENTITY_REFERENCE:
					dataTypeScript = "INT";
					dataDefaultValue = "0";
					break;
				case Entity.DATATYPE_DATE_BINARY:
					dataTypeScript = "INT";
					dataDefaultValue = Long.toString(Long.MIN_VALUE);
					break;
				case Entity.DATATYPE_DATE:
				case Entity.DATATYPE_TEXT:
				case Entity.DATATYPE_STRING:
				case Entity.DATATYPE_GEOLOCATION:
					dataTypeScript = "TEXT";
					dataDefaultValue = "''";
					break;
				case Entity.DATATYPE_REAL:
					dataTypeScript = "REAL";
					dataDefaultValue = "0";
					break;
				case Entity.DATATYPE_BLOB:
					dataTypeScript = "BLOB";
					dataDefaultValue = "0";
					break;
			}
		
			if (pMapping.DataBaseIsPrimaryKey) {
				dataPrimaryKeyScript = "PRIMARY KEY ";	
			}
			if (!pMapping.DataBaseAllowNulls) {
				dataAllowNullsScript = "NOT NULL ";
				dataDefaultValue = "DEFAULT " + dataDefaultValue;
			} else {
				dataDefaultValue = "";
			}
			
			returnedValue = String.format("%s %s %s %s %s", 
					dataFieldName,
					dataTypeScript,
					dataPrimaryKeyScript,
					dataAllowNullsScript,
					dataDefaultValue);
		}
		
		return returnedValue;
	}
	
	/**
	 * This method find a DataMapping definition into managed ObjectSet.
	 * @param pFieldName Database field name.
	 * @param pObjectSet Managed ObjectSet.
	 * @return Returns the DataMapping definition.
	 */
	private DataMapping getDataMapping(String pFieldName, ObjectSet<?> pObjectSet) {
		
		if (pFieldName != null && pFieldName.trim() != "") {
			if (pObjectSet != null) {
				if (pObjectSet.getDataMappings() != null && pObjectSet.getDataMappings().size() > 0) {
					for(DataMapping mapping : pObjectSet.getDataMappings()) {
						if (mapping.DataBaseFieldName.trim().toLowerCase().equals(pFieldName.trim().toLowerCase())) {
							return mapping;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param pDefinedFields Array with the defined fields into the entities model.
	 * @param pExistingfFields Array with the defined fields into the database table.
	 */
	private List<DatabaseMergeResult> mergeModels(String[] pDefinedFields, String[] pExistingfFields) {
		List<DatabaseMergeResult> returnedValue = new ArrayList<DatabaseMergeResult>();
		
		if (pDefinedFields != null && pDefinedFields.length > 0) {
			if (pExistingfFields != null && pExistingfFields.length > 0) {
				for(String modelFieldName : pDefinedFields) {
					DatabaseMergeResult result = new DatabaseMergeResult();
					result.FieldName = modelFieldName;
					result.Action = DatabaseMergeResult.ACTION_ADD;
					
					for(String databaseFieldName : pExistingfFields) {
						if (modelFieldName.trim().toLowerCase().equals(databaseFieldName.trim().toLowerCase())) {
							result.Action = DatabaseMergeResult.ACTION_NOTHING;
							break;
						}
					}
					
					returnedValue.add(result);
				}
				
				
				for(String databaseFieldName : pExistingfFields) {
					boolean fieldFound = false;
					for(String modelFieldName : pDefinedFields) {
						if (modelFieldName.trim().toLowerCase().equals(databaseFieldName.trim().toLowerCase())) {
							fieldFound = true;
							break;
						}
					}
					
					if (!fieldFound) {
						DatabaseMergeResult result = new DatabaseMergeResult();
						result.FieldName = databaseFieldName;
						result.Action = DatabaseMergeResult.ACTION_DELETE;
						
						returnedValue.add(result);
					}
				}
				
			}
		}
		
		return returnedValue;
	}
	
	/**
	 * This method check if the specified table exist into database schema.
	 * @param pTableName
	 * @return Returns true if the table exist and false if does not exist.
	 */
	private boolean tableExists(String pTableName) {
		boolean returnedValue = false;
		
		if (getDatabse() != null && getDatabse().isOpen()) {
			Cursor cursor = getDatabse().rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = ?", new String[] { pTableName });
			if (cursor != null) {
				try {
					cursor.moveToLast();
					cursor.moveToFirst();
					
					if (cursor.getCount() > 0) {
						returnedValue = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					cursor.close();
					cursor = null;
				}
			}
		}
		
		return returnedValue;
	}
	
	/**
	 * This method retrieve the existing database table names.
	 * @return Returns a array with the actual table names into the database.
	 */
	private String[] getTables() {
		String[] returnedValue = null;
		List<String> tablesList = new ArrayList<String>();
		
		if (getDatabse() != null && getDatabse().isOpen()) {
			Cursor cursor = getDatabse().rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master", null);
			if (cursor != null) {
				try {
					cursor.moveToLast();
					cursor.moveToFirst();
					
					if (cursor.getCount() > 0) {
						do{
							String tableName = cursor.getString(0);
							if (tableName != null && tableName.trim() != "") {
								if (!tableName.trim().toLowerCase().equals("android_metadata") &&
									!tableName.trim().toLowerCase().equals("sqlite_sequence")) {
								
									tablesList.add(tableName);
								}
							}
						} while(cursor.moveToNext());
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					cursor.close();
					cursor = null;
				}
			}
		}
		
		if (tablesList.size() > 0) {
			returnedValue = tablesList.toArray(new String[tablesList.size()]);
		}
		tablesList.clear();
		tablesList = null;
		
		return returnedValue;
	}
	
	/**
	 * This method retrieve the database table fields.
	 * @return Returns a array with the actual fields names into the database table.
	 */
	private String[] getTableFields(String pTableName) {
		String[] returnedValue = null;
		
		if (getDatabse() != null && getDatabse().isOpen()) {
			
			Cursor cursor = getDatabse().query(pTableName, null, null, null, null, null, null);
			if (cursor != null) {
				returnedValue = cursor.getColumnNames();
			}
			cursor.close();
			cursor = null;
			
		}
		
		return returnedValue;
	}
	
	/**
	 * This method retrieve the defined fields into the entities model.
	 * @param pObjectSet The instance of ObjsetSet to be managed.
	 * @return Array with the fields names.
	 */
	private String[] getDefinedFields(ObjectSet<?> pObjectSet) {
		String[] returnedValue = null;
		List<String> fieldsList = new ArrayList<String>();
		
		if (pObjectSet != null) {
			if (pObjectSet.getDataMappings().size() > 0) {
				for(DataMapping mapping : pObjectSet.getDataMappings()) {
					if (!mapping.virtual && !mapping.IsCollection) {
						if (mapping.DataBaseDataType != Entity.DATATYPE_ENTITY && mapping.DataBaseDataType != Entity.DATATYPE_ENTITY_LINK) {
							fieldsList.add(mapping.DataBaseFieldName);
						}
					}
				}
			}
		}
		
		if (fieldsList.size() > 0) {
			returnedValue = fieldsList.toArray(new String[fieldsList.size()]);
		}
		
		return returnedValue;
	}
}
