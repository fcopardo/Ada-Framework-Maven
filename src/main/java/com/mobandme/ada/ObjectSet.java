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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.util.Log;
import android.widget.ArrayAdapter;
import com.mobandme.ada.annotations.Table;
import com.mobandme.ada.annotations.TableField;
import com.mobandme.ada.annotations.TableIndex;
import com.mobandme.ada.annotations.TableIndexes;
import com.mobandme.ada.exceptions.AdaFrameworkException;
import com.mobandme.ada.exceptions.InaccessibleFieldException;
import com.mobandme.ada.exceptions.PopulateObjectSetException;
import com.mobandme.ada.listeners.ObjectSetEventsListener;


/**
 * Entity ObjectSet.
 * @version 2.4.3
 * @author Mob&Me
 */
@SuppressWarnings("serial")
public class ObjectSet<T extends Entity> extends ArrayList<T> implements List<T> {	
	private ObjectSet<Entity> ownerEntityType = null;
	private Class<?> managedType;
	private ObjectContext dataContext;
	private List<DataMapping> dataMappings = new ArrayList<DataMapping>();
	private String dataBaseTableName = "";
	private String dataBaseLinkedTableName = "";
	private String[] dataBaseTableFields = null;
	private String dataBaseUniqueTableFields = "";
	private ArrayAdapter<T> dataAdapter;
	private boolean deleteOnCascade = true;
	private List<ObjectSet<Entity>> inheritedObjectSets = new ArrayList<ObjectSet<Entity>>();
	private List<ObjectSet<Entity>> linkedObjectSets = new ArrayList<ObjectSet<Entity>>();
	private HashMap<String, List<DataIndex>> tableIndexes;
	private HashMap<Long, Entity> entitiesCache;
	
	private ObjectSetEventsListener objectSetEventsListener;
	private boolean dataBaseUseIndexes = DataUtils.DATABASE_USE_INDEXES;
	private boolean containsLinkedEntities = false;
	private boolean isLinkedSet = false;
	private boolean notifyAdapterChanges = true;
	
	public boolean isLinkedSet() { return isLinkedSet; }
	public void setLinkedSet(boolean isLinkedSet) { this.isLinkedSet = isLinkedSet; }
	
	public void enableAdapterNotifications() {
		enableAdapterNotifications(true);
	}
	
	public void enableAdapterNotifications(boolean pForceUpdate) {
		notifyAdapterChanges = true;
		
		if (pForceUpdate) {
			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}
	}
	
	public void disableAdapterNotifications() {
		notifyAdapterChanges = false;
	}
	
	/**
	 * This method retrieve if the ObjectSet contains relations with Linked Tables.
	 * @return
	 */
	boolean ContainsLinkedEntities() { return containsLinkedEntities; }

	/**
	 * This method set if the ObjectSet contains relations with Linked Tables.
	 * @param containsLinkedEntities
	 */
	void setContainsLinkedEntities(boolean containsLinkedEntities) { this.containsLinkedEntities = containsLinkedEntities; }
	
	ObjectContext getContext() {
		return this.dataContext;
	}
	
	ObjectSet<Entity> getOwnerEntityType() {
		return this.ownerEntityType;
	}
	
	List<DataMapping> getDataMappings() {
		return this.dataMappings;
	}
	
	/**
	 * Retrieve defined database table field names.
	 * @return String[] with the names of the Database table field names.
	 */
	public String[] getDataBaseTableFields() {
		return this.dataBaseTableFields;
	}
	
	
	/***
	 * Set the ObjectSet events listener.
	 * @param pListener
	 */
	public void setObjectSetEventsListener(ObjectSetEventsListener pListener) {
		this.objectSetEventsListener = pListener;
	}
	
	/**
	 * Get the ObjectSetEventsListener instance.
	 * @return
	 */
	public ObjectSetEventsListener getObjectSetEventsListener() {
		return this.objectSetEventsListener;
	}
	
	/**
	 * @return If the delete commands use cascade method.
	 */
	public boolean isDeleteOnCascade() {
		return deleteOnCascade;
	}

	/**
	 * Set if the delete actions use cascade method.
	 * @param deleteOnCascade
	 */
	public void setDeleteOnCascade(boolean deleteOnCascade) {
		this.deleteOnCascade = deleteOnCascade;
	}
	
	/**
	 * @return True if the managed entity contains other entities.
	 */
	protected final boolean ContainInheritedEntities() {
		boolean returnedValue = false;
		
		if (inheritedObjectSets != null) {
			if (inheritedObjectSets.size() > 0) {
				returnedValue = true;
			}
		}
		
		return returnedValue;
	}
	
	/**
	 * @return The list with the inherited fields.
	 */
	//protected final Enumeration<ObjectSet<Entity>> getInheritedObjectSets () { return this.inheritedObjectSets.elements(); }
	protected final List<ObjectSet<Entity>> getInheritedObjectSets () { return this.inheritedObjectSets; }
	
	/**
	 * Set the Data Adapter to notify DataSet Changes.
	 * @param pDataAdapter
	 */
	public void setAdapter(ArrayAdapter<T> pDataAdapter) {
		this.dataAdapter = pDataAdapter;
		initializeDataAdapter();
	}
	
	/**
	 * @return Class object of managed type for the Object Set.
	 */
	public Class<?> getManagedType() { return managedType; }
	
	/**
	 * @return DataBase table name.
	 */
	public String getDataBaseTableName() { return this.dataBaseTableName; }
	
	/**
	 * @return Linked table name.
	 */
	public String getDatabaseLinkedTableName() { return this.dataBaseLinkedTableName; }
	
	public List<String> getDataBaseTableIndexes() {
		return this.generateDataBaseTableIndexesScript(this.dataMappings);
	}
	/**
	 * @return DataBase table creation Script.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public String[] getDataBaseTableScript() throws AdaFrameworkException {
		return generateDataBaseTableScript(this.dataMappings);
	}
	
	
	/**
	 * Generate database Script for configured indexes.
	 * @return Indexes database scripts-
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public String[] getDataBaseTableIndexScript() throws AdaFrameworkException {
		return generateDataBaseTableIndexScript(this.tableIndexes);
	}
	
	/**
	 * Gets the database field name associated with the entity property.
	 * @param pFieldName Entity property name. 
	 * @return Database table field name.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public String getDataTableFieldName(String pFieldName) throws AdaFrameworkException {
		String returnedValue = null;
		
		try {
		
			if (this.dataMappings != null) {
				if (this.dataMappings.size() > 0) {
					for(DataMapping mapping : this.dataMappings) {
						if (mapping.EntityFieldName.equals(pFieldName)) {
							returnedValue = mapping.DataBaseFieldName;
							break;
						}
					}
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
		
		return returnedValue;
	}
	
	/**
	 * Gets the database field name associated with the entity property.
	 * @param pField Entity property.
	 * @return Database table field name.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public String getDataTableFieldName(Field pField) throws AdaFrameworkException {
		String returnedValue = null;
		
		try {
		
			if (pField != null) {
				TableField tableFieldAnnotation = pField.getAnnotation(TableField.class);
				
				if (tableFieldAnnotation != null) {
					returnedValue = tableFieldAnnotation.name();
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
		
		return returnedValue;
	}
	
	/**
	 * Principal constructor of the class.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public ObjectSet(Class<T> pManagedType, ObjectContext pContext) throws AdaFrameworkException { 
		 this(pManagedType, pContext, null);
	}
	
	public ObjectSet(Class<T> pManagedType, ObjectContext pContext, String pTableName) throws AdaFrameworkException { 
		try{
			
			this.ownerEntityType = null;
			this.dataContext = pContext;
			this.managedType = pManagedType;
			
			if (pTableName == null) {
				loadDataTableName(pManagedType, null, null, Entity.DATATYPE_EMPTY); //Always First;
		    } else {
				this.dataBaseTableName = pTableName;
			}
			
			this.dataMappings = loadDataMappings(this.managedType);
			this.dataBaseTableFields = loadDataBaseTableFields();
			
			if (pContext.getObjectContextEventsListener() != null) {
				setObjectSetEventsListener(pContext.getObjectContextEventsListener());
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
	
	/**
	 * Constructor of the class.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	ObjectSet(ObjectSet<Entity> pOwnerEntityType, DataMapping pMapping, ObjectContext pContext, Boolean pLinked) throws AdaFrameworkException {
		try{
			
			setLinkedSet(pLinked);
			this.ownerEntityType = pOwnerEntityType;
			this.dataContext = pContext;
			this.managedType = pMapping.EntityManagedType;
			
			loadDataTableName(pMapping.EntityManagedType, pMapping.DataBaseTableName, pMapping.DataBaseFieldName, pMapping.DataBaseDataType); //Always First;
			this.dataMappings = loadDataMappings(this.managedType); 
			this.dataBaseTableFields = loadDataBaseTableFields();
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
	 
	/**
	 * Constructor of the class.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	ObjectSet(ObjectSet<Entity> pOwnerEntityType, Class<T> pManagedType, ObjectContext pContext, List<DataMapping> pDataMapping, String[] pDataBaseTableFields, String pTableName, List<ObjectSet<Entity>> pInheritedObjectSets, Boolean pUseIndexes) throws AdaFrameworkException {
		try{
			
			this.ownerEntityType = pOwnerEntityType;
			this.dataContext = pContext;
			this.managedType = pManagedType;
			
			this.dataBaseUseIndexes = pUseIndexes;
			this.dataMappings = pDataMapping;
			this.dataBaseTableName = pTableName;
			this.dataBaseTableFields = pDataBaseTableFields;
			this.inheritedObjectSets = pInheritedObjectSets;
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
	
	/**
	 * Get all elements in the Entity Storage.
	 * @throws Exception 
	 */
	public void fill() throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, null, null, null, null, null, null, null, null, null);
	}
	
	/**
	 * Get N elements from the Entity Storage.
	 * @param pLimit Maximum number of elements.
	 * @throws Exception 
	 */
	public void fill(Integer pLimit) throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, null, null, null, null, null, null, pLimit, null, null);
	}
	/**
	 * Get N element from Y position from the Entity Storage.
	 * @param pOffset Start position.
	 * @param pLimit Maximum number of elements.
	 * @throws Exception
	 */
	public void fill(Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, null, null, null, null, null, pOffset, pLimit, null, null);
	}
	
	/**
	 * Get all elements from the Entity Storage ordered by the Sort Expression.
	 * @param pOrderBy Sort Expression.
	 * @throws Exception 
	 */
	public void fill(String pOrderBy) throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, null, null, pOrderBy, null, null, null, null, null, null);
	}
	
	/**
	 * Get N elements from the Entity Storage ordered by the Sort Expression.
	 * @param pOrderBy Sort Expression.
	 * @param pLimit Maximum number of elements.
	 * @throws Exception 
	 */
	public void fill(String pOrderBy, Integer pLimit) throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, null, null, pOrderBy, null, null, null, pLimit, null, null);
	}
	
	/**
	 * Get N elements with the Y offset from the Entity Storage ordered by the Sort Expression.
	 * @param pOrderBy Sort Expression.
	 * @param pOffset Start position.
	 * @param pLimit Maximum number of elements.
	 * @throws Exception 
	 */
	public void fill(String pOrderBy, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, null, null, pOrderBy, null, null, pOffset, pLimit, null, null);
	}
	
	/**
	 * Get all elements from the Entity Storage ordered by the Sort Expression, and filter with the Where arguments values.
	 * @param pWherePattern Where clause pattern.
	 * @param pWhereValues  Where clause values.
	 * @param pOrderBy Sort Expression.
	 * @throws Exception 
	 */
	public void fill(String pWherePattern, String[] pWhereValues, String pOrderBy) throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, pWherePattern, pWhereValues, pOrderBy, null, null, null, null, null, null);
	}
	public void fill(SQLiteDatabase pDatabase, String pWherePattern, String[] pWhereValues, String pOrderBy) throws AdaFrameworkException {
		fillList(pDatabase, dataBaseTableFields, dataBaseTableName, false, pWherePattern, pWhereValues, pOrderBy, null, null, null, null, null, null);
	}
	void fill(SQLiteDatabase pDatabase, String pWherePattern, String[] pWhereValues, String pOrderBy, Entity pParent) throws AdaFrameworkException {
		fillList(pDatabase, dataBaseTableFields, dataBaseTableName, false, pWherePattern, pWhereValues, pOrderBy, null, null, null, null, null, pParent);
	}
	
	/**
	 * Get N elements from the Entity Storage ordered by the Sort Expression, and filter with the Where arguments values.
	 * @param pWherePattern Where clause pattern.
	 * @param pWhereValues  Where clause values.
	 * @param pOrderBy Sort Expression.
	 * @param pLimit Maximum number of Entities.
	 * @throws Exception 
	 */
	void fill(SQLiteDatabase pDatabase, String pWherePattern, String[] pWhereValues, String pOrderBy, Integer pLimit, Entity pParent) throws AdaFrameworkException {
		fillList(pDatabase, dataBaseTableFields, dataBaseTableName, false, pWherePattern, pWhereValues, pOrderBy, null, null, null, pLimit, null, pParent);
	}
	
	public void fill(String pWherePattern, String[] pWhereValues, String pOrderBy, Integer pLimit) throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, pWherePattern, pWhereValues, pOrderBy, null, null, null, pLimit, null, null);
	}
	
	/**
	 * Get N elements  with Y offset from the Entity Storage ordered by the Sort Expression, and filter with the Where arguments values.
	 * @param pWherePattern Where clause pattern.
	 * @param pWhereValues  Where clause values.
	 * @param pOrderBy Sort Expression.
	 * @param pOffset Start position.
	 * @param pLimit Maximum number of Entities.
	 * @throws Exception 
	 */
	public void fill(String pWherePattern, String[] pWhereValues, String pOrderBy, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		fillList(dataBaseTableFields, dataBaseTableName, false, pWherePattern, pWhereValues, pOrderBy, null, null, pOffset, pLimit, null, null);
	}
	
	/**
	 * Get N elements  with Y offset from the Entity Storage ordered by the Sort Expression, and filter with the Where arguments values.
	 * @param pTableName Custom database table name.
	 * @param pWherePattern Where clause pattern.
	 * @param pWhereValues  Where clause values.
	 * @param pOrderBy Sort Expression.
	 * @param pOffset Start position.
	 * @param pLimit Maximum number of Entities.
	 * @throws Exception 
	 */
	public void fill(String pTableName, String pWherePattern, String[] pWhereValues, String pOrderBy, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		fillList(dataBaseTableFields, pTableName, false, pWherePattern, pWhereValues, pOrderBy, null, null, pOffset, pLimit, null, null);
	}
	
	/**
	 * Get N elements  with Y offset from the Entity Storage ordered by the Sort Expression, and filter with the Where arguments values.
	 * @param pTableFields Custom database table fields.
	 * @param pTableName Custom database table name.
	 * @param pWherePattern Where clause pattern.
	 * @param pWhereValues  Where clause values.
	 * @param pOrderBy Sort Expression.
	 * @param pOffset Start position.
	 * @param pLimit Maximum number of Entities.
	 * @throws Exception 
	 */
	public void fill(String[] pTableFields, String pTableName, String pWherePattern, String[] pWhereValues, String pOrderBy, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		fillList(pTableFields, pTableName, false, pWherePattern, pWhereValues, pOrderBy, null, null, pOffset, pLimit, null, null);
	}
	
	/**
	 * Parse and log Query sentence.
	 * @param pDistinct
	 * @param pTableName
	 * @param pFields
	 * @param pWherePattern
	 * @param pWhereValues
	 * @param pOrderBy
	 * @param pGroupBy
	 * @param pHaving
	 * @param pLimit
	 */
	private void logQuery(String pTotalTime, Boolean pDistinct, String pTableName, String[] pFields, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, String pLimit) {
		try {
			boolean debugable = getContext().isOnDebugMode();
			
			if (debugable) {
				String sQuery = "SELECT";
				if (pDistinct != null && pDistinct) {
					sQuery += " DISTINCT";
				}
				
				String sQueryFields = "";
				for(String sField : pFields) {
					if (sQueryFields != "") {
						sQueryFields += ", ";
					}
					sQueryFields += sField;
				}
				sQuery += " " + sQueryFields;
				sQuery += " FROM " + pTableName;
				
				if (pWherePattern != null) {
					if (pWherePattern != "") {
						sQuery += " WHERE " + pWherePattern;
					}
				}
				if (pWhereValues != null) {
					if (pWhereValues.length > 0){
						sQuery = String.format(sQuery.replace("?", "%s"), (Object)pWhereValues);
					}
				}
				
				if (pOrderBy != null) {
					sQuery += " ORDER BY " + pOrderBy;
				}
				if (pLimit != null) {
					sQuery += " LIMIT " + pLimit;
				}
				
				Log.d(DataUtils.DEFAULT_LOGS_TAG, pTotalTime + ": " + sQuery);
			}
		} catch (Exception e) {
			ExceptionsHelper.manageException(e.toString());
		}
	}
	
	/**
	 * Populate internal Entities List.
	 * @param pDistinct
	 * @param pWherePattern
	 * @param pWhereValues
	 * @param pOrderBy
	 * @param pGroupBy
	 * @param pHaving
	 * @param pOffset
	 * @param pLimit
	 * @param pOwnerID
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	void fillList(String[] pTableFields, String pTableName, Boolean pDistinct, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit, Integer pOwnerID, Entity pParent) throws AdaFrameworkException {
		fillList(null, pTableFields, pTableName, pDistinct, pWherePattern, pWhereValues, pOrderBy, pGroupBy, pHaving, pOffset, pLimit, pOwnerID, pParent);
	}
		
	void fillList(SQLiteDatabase pDataBase, String[] pTableFields, String pTableName, Boolean pDistinct, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit, Integer pOwnerID, Entity pParent) throws AdaFrameworkException {
		Boolean manageDatabase = false;
		SQLiteDatabase database = pDataBase;
		
		try {
			
			beforeFill();
			
			if (database == null) {
				database = this.dataContext.getReadableDatabase();
				manageDatabase = true;
			}
			
			if (!getContext().isLazyLoading()) {
				fillFullList(database, pTableFields, pTableName, pDistinct, pWherePattern, pWhereValues, pOrderBy, pGroupBy, pHaving, pOffset, pLimit, pOwnerID, pParent);
			} else {
				fillLazyList(database, pDistinct, pWherePattern, pWhereValues, pOrderBy, pGroupBy, pHaving, pOffset, pLimit, pOwnerID, pParent);
			}

			afterFill();
			
		} catch (Exception e) { 
			ExceptionsHelper.manageException(this, e);
		} finally {
			if (manageDatabase) {
				if (database != null) {
					if (database.isOpen()) {
						database.close();
					}
					database = null;
				}	
			}
			
			if (this.objectSetEventsListener != null) {
				if (isContextActivity()) {
					((Activity)dataContext.getContext()).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							objectSetEventsListener.OnFillComplete(ObjectSet.this);
						}
					});
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	void fillFullList(SQLiteDatabase pDataBase, String[] pTableFields, String pTableName, Boolean pDistinct, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit, Integer pOwnerID, Entity pParent) throws AdaFrameworkException {
		Date initOfProcess = new Date();
		Cursor entitiesCursor = null;
		SQLiteDatabase database = pDataBase;
		
		try {
			
			String[] queryFields = pTableFields;
			String whereFormat = pWherePattern;
			String whereValues[] = pWhereValues;
			String orderBy = pOrderBy;
			String groupBy = pGroupBy;
			String having = pHaving;
			String limit = null;
			boolean distinct = false;
			if (pDistinct != null) {
				distinct = pDistinct;
			}
			
			if ((pLimit != null) && (pOffset != null)) {
				limit = String.format(DataUtils.DATABASE_LIMIT_OFFTSET_PATTERN, pOffset, pLimit);
			} else if (pLimit != null) {
				limit = String.format(DataUtils.DATABASE_LIMIT_PATTERN, pLimit);
			}
			if (orderBy == null) {
				orderBy = DataUtils.DATABASE_ID_FIELD_NAME + " ASC";
			}
			if (queryFields == null || queryFields.length <= 0) {
				queryFields =  dataBaseTableFields;
			}
			if (database != null) {			
				this.clear();		
				
				String tableName = pTableName;
				if (tableName == null || tableName.equals("")) {
					tableName = dataBaseTableName;
				}
				entitiesCursor = getContext().executeQuery(database, distinct, tableName, queryFields, whereFormat, whereValues, groupBy, having, orderBy, limit);
				
				if (entitiesCursor != null) {
					entitiesCursor.moveToLast();
					entitiesCursor.moveToFirst();
					
					int numberOfElements = entitiesCursor.getCount();
					if (numberOfElements > 0) {
						do{
							Entity entity = generateNewEntity(database, entitiesCursor, pParent);
							
							if (entity != null) {
								if (isValidElement((T)entity)) 
									this.add((T)entity);
							}
						} while(entitiesCursor.moveToNext());
					}
				}
			}
			
			//Print the LogCat Log.
			Date endOfProcess = new Date();
			String totalTime = DataUtils.calculateTimeDiference(initOfProcess, endOfProcess);
			logQuery(totalTime, pDistinct, this.dataBaseTableName, this.dataBaseTableFields,  whereFormat, whereValues, orderBy, groupBy, having, limit);
			
		} catch (Exception e) {
			throw new PopulateObjectSetException(e);
		} finally {
			if (entitiesCursor != null) {
				entitiesCursor.close();
				entitiesCursor = null;
			}
		}
	}
	
	/**
	 * This method validate the Entity before to add to ObjectSet.
	 * @param pEntity
	 * @return
	 */
	protected boolean isValidElement(T pEntity) {
		return true;
	}
	
	protected void beforeFill() { }
	
	protected void afterFill() { }
	
	@SuppressWarnings("unchecked")
	void fillLazyList(SQLiteDatabase pDataBase, Boolean pDistinct, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit, Integer pOwnerID, Entity pParent)  throws AdaFrameworkException {
		Date initOfProcess = new Date();
		Cursor entitiesCursor = null;
		SQLiteDatabase database = pDataBase;
		
		try {
			
			String whereFormat = pWherePattern;
			String whereValues[] = pWhereValues;
			String orderBy = pOrderBy;
			String groupBy = pGroupBy;
			String having = pHaving;
			String limit = null;
			boolean distinct = false;
			if (pDistinct != null) {
				distinct = pDistinct;
			}
			
			if ((pLimit != null) && (pOffset != null)) {
				limit = String.format(DataUtils.DATABASE_LIMIT_OFFTSET_PATTERN, pOffset, pLimit);
			} else if (pLimit != null) {
				limit = String.format(DataUtils.DATABASE_LIMIT_PATTERN, pLimit);
			}
			if (orderBy == null) {
				orderBy = DataUtils.DATABASE_ID_FIELD_NAME + " ASC";
			}
			
			if (database != null) {			
				this.clear();		
				
				entitiesCursor = getContext().executeQuery(database, distinct, this.dataBaseTableName, new String[] { DataUtils.DATABASE_ID_FIELD_NAME }, whereFormat, whereValues, groupBy, having, orderBy, limit);
				
				if (entitiesCursor != null) {
					entitiesCursor.moveToLast();
					entitiesCursor.moveToFirst();
					
					int numberOfElements = entitiesCursor.getCount();
					if (numberOfElements > 0) {
						do{
							Entity entity = generateNewEntity(database, entitiesCursor, pParent);
							
							if (entity != null) {
								entity.setLazyLoaded(false);
								this.add((T)entity);
							}
						} while(entitiesCursor.moveToNext());
					}
				}
			}
			
			//Print the LogCat Log.
			Date endOfProcess = new Date();
			String totalTime = DataUtils.calculateTimeDiference(initOfProcess, endOfProcess);
			logQuery(totalTime, pDistinct, this.dataBaseTableName, this.dataBaseTableFields,  whereFormat, whereValues, orderBy, groupBy, having, limit);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new PopulateObjectSetException(e);
		} finally {
			if (entitiesCursor != null) {
				if (!entitiesCursor.isClosed()) {
					entitiesCursor.close();
				}
				entitiesCursor = null;
			}
		}
	}
	
	
	/****************************************/
	/*		SEARCH METHODS 					*/
	/****************************************/
	
	/**
	 * Get a list of entities unlinked of owner ObjectSet.
	 * @param pDistinct
	 * @param pWherePattern
	 * @param pWhereValues
	 * @param pOrderBy
	 * @param pGroupBy
	 * @param pHaving
	 * @param pOffset
	 * @param pLimit
	 * @return List of entities.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public List<T> search(Boolean pDistinct, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		return searchList(null, this.dataBaseTableName, pDistinct, this.dataBaseTableFields, pWherePattern, pWhereValues, pOrderBy, pGroupBy, pHaving, pOffset, pLimit);
	}
	
	public List<T> search(Boolean pDistinct, String pTableName, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		return searchList(null, pTableName, pDistinct, this.dataBaseTableFields, pWherePattern, pWhereValues, pOrderBy, pGroupBy, pHaving, pOffset, pLimit);
	}
	
	/**
	 * Get a list of entities unlinked of owner ObjectSet.
	 * @param pDistinct
	 * @param pFields
	 * @param pWherePattern
	 * @param pWhereValues
	 * @param pOrderBy
	 * @param pGroupBy
	 * @param pHaving
	 * @param pOffset
	 * @param pLimit
	 * @return List of entities.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public List<T> search(Boolean pDistinct, String[] pFields, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		return searchList(null, this.dataBaseTableName, pDistinct, pFields, pWherePattern, pWhereValues, pOrderBy, pGroupBy, pHaving, pOffset, pLimit);
	}
	
	public List<T> search(String pTableName, Boolean pDistinct, String[] pFields, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		return searchList(null, pTableName, pDistinct, pFields, pWherePattern, pWhereValues, pOrderBy, pGroupBy, pHaving, pOffset, pLimit);
	}
	
	@SuppressWarnings("unchecked")
	List<T> searchList(SQLiteDatabase pDataBase, String pTableName, Boolean pDistinct, String[] pFields, String pWherePattern, String[] pWhereValues, String pOrderBy, String pGroupBy, String pHaving, Integer pOffset, Integer pLimit) throws AdaFrameworkException {
		List<T> returnedValue = null;
		Date initOfProcess = new Date();
		Boolean manageDatabase = false;
		SQLiteDatabase database = pDataBase;
		Cursor entitiesCursor = null;
		
		try {
					
			beforeFill();
			
			String whereFormat = pWherePattern;
			String whereValues[] = pWhereValues;
			String orderBy = pOrderBy;
			String groupBy = pGroupBy;
			String having = pHaving;
			String limit = null;
			boolean distinct = false;
			if (pDistinct != null) {
				distinct = pDistinct;
			}
			
			if ((pLimit != null) && (pOffset != null)) {
				limit = String.format(DataUtils.DATABASE_LIMIT_OFFTSET_PATTERN, pOffset, pLimit);
			} else if (pLimit != null) {
				limit = String.format(DataUtils.DATABASE_LIMIT_PATTERN, pLimit);
			}
			if (orderBy == null) {
				orderBy = DataUtils.DATABASE_ID_FIELD_NAME + " ASC";
			}
			
			if (database == null) {
				database = this.dataContext.getReadableDatabase();
				manageDatabase = true;
			}
			
			if (database != null) {				
				entitiesCursor = getContext().executeQuery(database, distinct, pTableName, pFields, whereFormat, whereValues, groupBy, having, orderBy, limit);
				
				if (entitiesCursor != null) {
					entitiesCursor.moveToLast();
					entitiesCursor.moveToFirst();
					
					int numberOfElements = entitiesCursor.getCount();
					if (numberOfElements > 0) {
						returnedValue = new ArrayList<T>();
						
						do{
							Entity entity = generateNewEntity(database, entitiesCursor, null);
							
							if (entity != null) {
								if (isValidElement((T)entity))
									returnedValue.add((T)entity);
							}
						} while(entitiesCursor.moveToNext());
					}
				}
			}
			
			
			Date endOfProcess = new Date();
			String totalTime = DataUtils.calculateTimeDiference(initOfProcess, endOfProcess);
			
			logQuery(totalTime, pDistinct, pTableName, pFields, whereFormat, whereValues, orderBy, groupBy, having, limit);
			
			afterFill();
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		} finally {
			if (entitiesCursor != null) {
				entitiesCursor.close();
				entitiesCursor = null;
			}
			
			if (manageDatabase) {
				if (database != null) {
					if (database.isOpen()) {
						database.close();
					}
					database = null;
				}
			}
		}
		
		return returnedValue;
	}
	
	
	/**
	 * Get element by ID from the Entities Storage.
	 * @param pId ID to find.
	 * @return Entity.
	 * @throws Exception
	 */
	public T getElementByID(Long pId) throws AdaFrameworkException {
		return getElementByID((SQLiteDatabase)null, pId);
	}
	
	public T getElementByID(String pTableName, Long pId) throws AdaFrameworkException {
		return getElementByID(null, pTableName, pId);
	}
	
	public T getElementByID(SQLiteDatabase pDatabase, Long pId) throws AdaFrameworkException {
		return getElementByID(pDatabase, pId, null);
	}
	
	public T getElementByID(SQLiteDatabase pDatabase, String pTableName, Long pId) throws AdaFrameworkException {
		return getElementByID(pDatabase, pTableName, pId, null);
	}
	
	public T getElementByID(SQLiteDatabase pDatabase, Long pId, Entity pParent) throws AdaFrameworkException {
		return getElementByID(pDatabase, this.dataBaseTableName, pId, null);
	}
	
	@SuppressWarnings({ "unchecked" })
	public T getElementByID(SQLiteDatabase pDatabase, String pTableName, Long pId, Entity pParent) throws AdaFrameworkException {
		T returnedValue = null;
		SQLiteDatabase database = pDatabase;
		Cursor entitiesCursor = null;
		Boolean manageDatabase = false;
		
		try {

			if (entitiesCache == null) {
				entitiesCache = new HashMap<Long, Entity>();
			}
			
			if (entitiesCache.containsKey(pId)) {
				returnedValue = (T)entitiesCache.get(pId);
			} else {
				String whereFormat = DataUtils.DATABASE_ID_FIELD_NAME + "=?";
				String[] whereValues = new String[] { Long.toString(pId)  };

				if (database == null) {
					manageDatabase = true;
					database = this.dataContext.getReadableDatabase();
				}
				entitiesCursor = getContext().executeQuery(database, false, pTableName, this.dataBaseTableFields, whereFormat, whereValues, null, null, null, null);
				
				if (entitiesCursor != null) {
					entitiesCursor.moveToLast();
					entitiesCursor.moveToFirst();
					
					if (entitiesCursor.getCount() > 0) {
						returnedValue = (T)generateNewEntity(database, entitiesCursor, pParent);
					
						if (returnedValue != null) {
							entitiesCache.put(returnedValue.getID(), returnedValue);
						}
					}
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		} finally {
			if (entitiesCursor != null) {
				entitiesCursor.close();
				entitiesCursor = null;
			}
			
			if (manageDatabase) {
				if (database != null) {
					if (database.isOpen()) {
						database.close();
					}
				}
				database = null;
			}
		}
		
		return returnedValue;
	}
	
	/**
	 * Save the Entities collection into the Entities Storage.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public synchronized void save() throws AdaFrameworkException {
		SQLiteDatabase database = null;
		
		Date initOfProcess = new Date();
		
		try {
		
			database = this.dataContext.getWritableDatabase();
			if (database != null) {
				if (this.dataContext.isUseTransactions()) {
					//Init the DataBase transaction.
					database.beginTransaction();
				}
			
				save(database, null);
				
				if (this.dataContext.isUseTransactions()) {
					//Make commit into active transaction.
					database.setTransactionSuccessful();
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		} finally {
			if (database != null) {
				if (this.dataContext.isUseTransactions()) {
					if (database.inTransaction()) {
						database.endTransaction();
					}
				}
				if (database.isOpen()) {
					database.close();
				}
				database = null;
			}
			
			if (isContextActivity()) {
				((Activity)dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
						
						if (objectSetEventsListener != null) {
							objectSetEventsListener.OnSaveComplete(ObjectSet.this);
						}
					}
				});
			}
		}
		
		Date endOfProcess = new Date();
		String totalTime = DataUtils.calculateTimeDiference(initOfProcess, endOfProcess);
		
		Log.d(DataUtils.DEFAULT_LOGS_TAG, String.format("TOTAL Time to execute Save '%s' command: %s.", this.managedType.getSimpleName(), totalTime));
	}
	
	/**
	 * Save the Entities collection into the Entities Storage.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public synchronized void save(SQLiteDatabase pDataBase) throws AdaFrameworkException {
		
		try{
			
			if (pDataBase.isOpen()) {
				save(pDataBase, null);
			} else {
				save();
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
		
		if (isContextActivity()) {
			((Activity)dataContext.getContext()).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
					
					if (objectSetEventsListener != null) {
						objectSetEventsListener.OnSaveComplete(ObjectSet.this);
					}
				}
			});
		}
	}
	
	/**
	 * Save Entities into DataBase.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	void save(SQLiteDatabase pDataBase, Long pOwnerID) throws AdaFrameworkException {
		SQLiteDatabase database = pDataBase;
		int index = 0;
		
		if (this.size() > 0) {
			if (database != null) {
				for(; index < this.size(); index++) {
					Entity entity = this.get(database, index, false);
					
					switch(entity.getStatus()) {
						case Entity.STATUS_NEW:
							saveNewEntity(database, entity, pOwnerID);
							saveInheritedEntities(database, entity);
							saveLinkedEntities(database, entity);
							break;
						case Entity.STATUS_UPDATED:
							entity = this.get(database, index, true);
							saveUpdatedEntity(database, entity, pOwnerID);
							saveInheritedEntities(database, entity);
							saveLinkedEntities(database, entity);
							break;
						case Entity.STATUS_DELETED:
							saveDeletedLinkedEntity(database, entity);
							saveDeletedEntity(database, entity, pOwnerID);
							if (index > 0){
								index--;
							}
							break;
					}
					
					if (this.objectSetEventsListener != null) {
						if (isContextActivity()) { 
							final int currentIndex = index;
							
							((Activity)dataContext.getContext()).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									objectSetEventsListener.OnSaveProgress(ObjectSet.this, currentIndex, ObjectSet.this.size());
								}
							});
						}
					}
				}
				
				if (this.size() > 0) {
					if (index == this.size()){
						Entity entity = this.get(database, (index - 1), false);
						
						switch(entity.getStatus()) {
							case Entity.STATUS_NEW:
								saveNewEntity(database, entity, pOwnerID);
								saveInheritedEntities(database, entity);
								saveLinkedEntities(database, entity);
								break;
							case Entity.STATUS_UPDATED:
								entity = this.get(database, (index - 1), true);
								saveUpdatedEntity(database, entity, pOwnerID);
								saveInheritedEntities(database, entity);
								saveLinkedEntities(database, entity);
								break;
							case Entity.STATUS_DELETED:
								saveDeletedLinkedEntity(database, entity);
								saveDeletedEntity(database, entity, pOwnerID);
								break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Save the passed Entity into Entities Storage.
	 * @param pEntity Entity object to be saved.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public void save(T pEntity) throws AdaFrameworkException {
		SQLiteDatabase database = null;
		
		Date initOfProcess = new Date();
		
		try {
		
			database = this.dataContext.getWritableDatabase();
			if (database != null) {
				if (this.dataContext.isUseTransactions()) {
					//Init the DataBase transaction.
					database.beginTransaction();
				}
			
				save(database, pEntity, null);
				
				if (this.dataContext.isUseTransactions()) {
					//Make commit into active transaction.
					database.setTransactionSuccessful();
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		} finally {
			if (database != null) {
				if (this.dataContext.isUseTransactions()) {
					if (database.inTransaction()) {
						database.endTransaction();
					}
				}
				if (database.isOpen()) {
					database.close();
				}
				database = null;
			}
			
			if (isContextActivity()) {
				((Activity)dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
						
						if (objectSetEventsListener != null) {
							objectSetEventsListener.OnSaveComplete(ObjectSet.this);
						}
					}
				});
			}
		}
		
		Date endOfProcess = new Date();
		String totalTime = DataUtils.calculateTimeDiference(initOfProcess, endOfProcess);
		Log.d(DataUtils.DEFAULT_LOGS_TAG, String.format("TOTAL Time to execute Save '%s' command: %s.", this.managedType.getSimpleName(), totalTime));
	}
	
	/**
	 * Save Entities into DataBase.
	 * @throws Exception 
	 */
	void save(SQLiteDatabase pDataBase, T pEntity, Long pOwnerID) throws AdaFrameworkException {
		SQLiteDatabase database = pDataBase;
		
		if (database != null) {
			switch(pEntity.getStatus()) {
				case Entity.STATUS_NEW:
					saveNewEntity(pDataBase, pEntity, pOwnerID);
					saveInheritedEntities(pDataBase, pEntity);
					saveLinkedEntities(pDataBase, pEntity);
					break;
				case Entity.STATUS_UPDATED:
					saveUpdatedEntity(pDataBase, pEntity, pOwnerID);
					saveInheritedEntities(pDataBase, pEntity);
					saveLinkedEntities(pDataBase, pEntity);
					break;
				case Entity.STATUS_DELETED:
					saveDeletedLinkedEntity(pDataBase, pEntity);
					saveDeletedEntity(pDataBase, pEntity, pOwnerID);
					break;
			}
		}
	}
	
	/**
	 * Initialize DataSet DataAdapter.
	 */
	private void initializeDataAdapter() {
		if (this.dataAdapter != null) {
			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dataAdapter.clear();
						
						if (size() > 0) {
							for (T entity : ObjectSet.this) {
								dataAdapter.add(entity);
							}
							notifyDataSetChanged();
						}					
					}
				});
			}
		}
	}
	
	/**
	 * Get the InheritedObjectSet instance.
	 * @param pType
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ObjectSet<Entity> getInheritedObjectSet(Class<?> pType, DataMapping pMapping) throws AdaFrameworkException {
		ObjectSet<Entity> inheritedObjectSet = null;
		ObjectSet<Entity> commonInheritedObjectSet = null;
		
		if (inheritedObjectSets != null && inheritedObjectSets.size() > 0) {
			for(ObjectSet<Entity> inheritedSet : inheritedObjectSets) {
				if (inheritedSet.getManagedType() == pType) {
					if (!inheritedSet.isLinkedSet()) {
						if (inheritedSet.getDataBaseTableName().equals(pMapping.DataBaseTableName)) {
							commonInheritedObjectSet = inheritedSet;
							break;
						}
					} else {
						if (inheritedSet.getDatabaseLinkedTableName().equals(pMapping.DataBaseMiddleTableName)) {
							commonInheritedObjectSet = inheritedSet;
							break;
						}
					}
				}
			}
		}
		
		if (commonInheritedObjectSet != null){
			inheritedObjectSet = new ObjectSet<Entity>(commonInheritedObjectSet.getOwnerEntityType(), (Class)commonInheritedObjectSet.getManagedType(), commonInheritedObjectSet.getContext(), commonInheritedObjectSet.getDataMappings(), commonInheritedObjectSet.getDataBaseTableFields(), commonInheritedObjectSet.getDataBaseTableName(), commonInheritedObjectSet.inheritedObjectSets, commonInheritedObjectSet.dataBaseUseIndexes);
		}
		
		return inheritedObjectSet;
	}
	
	@SuppressWarnings("unchecked")
	private ObjectSet<Entity> getLinkedObjectSet(Class<?> pType, DataMapping pMapping) throws AdaFrameworkException {
		ObjectSet<Entity> linkedObjectSet = null;
		
		if (linkedObjectSets != null && linkedObjectSets.size() > 0) {
			for(ObjectSet<Entity> linkedSet : inheritedObjectSets) {
				if (linkedSet.getManagedType() == pType) {
					if (linkedSet.getDatabaseLinkedTableName().equals(pMapping.DataBaseMiddleTableName)) {
						linkedObjectSet = linkedSet;
						break;
					}
				}
			}
		}
		
		if (linkedObjectSet == null){
			linkedObjectSet = new ObjectSet<Entity>((Class<Entity>)pType, getContext());
			linkedObjectSets.add(linkedObjectSet);
		}
		
		return linkedObjectSet;
	}
	
	/**
	 * This method extract the database table name from the Annotations.
	 * @param pManagedType
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private void loadDataTableName(Class<?> pManagedType, String pOwnerTableName, String pOwnerFieldNane, int pDataType) {
		if (pManagedType != null) {
			if (pManagedType != Entity.class) {
				Class<?> type = pManagedType;
				
				while((type != Entity.class || type == Object.class)) {
					if (type != null) {
						//Get the DataBase table name from the Entity class metadata.
						Table tableAnnotation = (Table)type.getAnnotation(Table.class);
						if (tableAnnotation != null) {
							if (this.dataBaseTableName == "") {
								this.dataBaseTableName = tableAnnotation.name();
							}
							
							this.dataBaseUseIndexes = tableAnnotation.useIndexes();
						}
					}
					
					type = type.getSuperclass();
					if (type == null) {
						break;
					}
				}
			}
		}
		
		//If Table Name not found assume the name of the ObjectSet managed Type.
		if (this.dataBaseTableName.trim().equals("")) 
			this.dataBaseTableName = pManagedType.getSimpleName();
		
		switch (pDataType) {
			case Entity.DATATYPE_ENTITY_LINK:		
				this.dataBaseLinkedTableName = String.format(DataUtils.DATABASE_LINKED_TABLE_NAME_PATTERN, pOwnerTableName, pOwnerFieldNane, this.dataBaseTableName);
				break;
			case Entity.DATATYPE_ENTITY_REFERENCE:
				break;
			default:
				if (pOwnerFieldNane != null && !pOwnerFieldNane.trim().equals("")) 
					this.dataBaseTableName = String.format("%s_%s", pOwnerFieldNane, this.dataBaseTableName);
				if (pOwnerTableName != null && !pOwnerTableName.trim().equals("")) 
					this.dataBaseTableName = String.format("%s_%s", pOwnerTableName, this.dataBaseTableName);
				break;
		}
	}
	
	/**
	 * Load the DataBase fields mapping.
	 * @throws Exception 
	 */
	private List<DataMapping> loadDataMappings(Class<?> pManagedType) throws AdaFrameworkException {
		List<DataMapping> returnedValue = new ArrayList<DataMapping>();
		
		try {
			Class<?> managedType = pManagedType;
			
			if (managedType != Entity.class) {
				while((managedType != Entity.class || managedType == Object.class)) {
					if (managedType != null) {						
						//Get all declared fields in the managed Object.
						Field[] declaredFields = managedType.getDeclaredFields();
						if (declaredFields != null) {
							extractDataMappings(declaredFields, returnedValue, false);
						}
					}
					
					managedType = managedType.getSuperclass();
					if (managedType == null) {
						break;
					}
				}
				
				if (managedType != null) {
					if (managedType == Entity.class) {
						if (this.ownerEntityType != null) {
							Field[] ownerFieldId = new Field[] { Entity.class.getDeclaredField("ID") };
							extractDataMappings(ownerFieldId, returnedValue, true);
						}
	
						//Get all declared fields in the managed Object.
						Field[] declaredFields = managedType.getDeclaredFields();
						if (declaredFields != null) {
							extractDataMappings(declaredFields, returnedValue, false);
						}
					}
				}
			}
			
			/*
			//If Table Name not found assume the name of the ObjectSet managed Type.
			if (this.dataBaseTableName == "") {
				this.dataBaseTableName = this.getManagedType().getSimpleName();
			}
			*/
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
		return returnedValue;
	}
	
	/**
	 * Extract Data Mappings from the Class Fields and it's added to the data mappings list.
	 * @param pDeclaredFields
	 * @param pMappingsList
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	private void extractDataMappings(Field[] pDeclaredFields, List<DataMapping> pMappingsList, Boolean pForeignKeys) throws AdaFrameworkException {
		Field[] declaredFields = pDeclaredFields;
		
		try {
			if (declaredFields != null) {
				if (declaredFields.length > 0) {
					//Look at all elements of the list of Fields
					for(Field declaredField : declaredFields) {
						//Get TableField Annotation object from the Class field definition.
						TableField tableFieldAnnotation = declaredField.getAnnotation(TableField.class);
						
						if (tableFieldAnnotation != null) {
							DataMapping dataMapping = new DataMapping();
							dataMapping.ForeignKey = pForeignKeys;
							dataMapping.EntityManagedType = declaredField.getType();
							dataMapping.EntityManagedField = declaredField;
							dataMapping.EntityFieldName = declaredField.getName();
							dataMapping.DataBaseTableName = this.dataBaseTableName;
							dataMapping.DataBaseFieldName = tableFieldAnnotation.name();
							dataMapping.DataBaseLength = tableFieldAnnotation.maxLength();
							dataMapping.DataBaseAllowNulls = !tableFieldAnnotation.required();
							dataMapping.DataBaseDataType = tableFieldAnnotation.datatype();
							dataMapping.DataBaseIsPrimaryKey = tableFieldAnnotation.isPrimaryKey();
							dataMapping.Encrypted = tableFieldAnnotation.encripted();
							dataMapping.Unique = tableFieldAnnotation.unique();
							dataMapping.BitmapCompression = tableFieldAnnotation.BitmapCompression();
							dataMapping.virtual = tableFieldAnnotation.virtual();
							
							//Getter And Setter Management
							dataMapping.setterMethod = ReflectionHelper.extractSetterMethod(this.managedType, dataMapping.EntityManagedField);
							dataMapping.getterMethod = ReflectionHelper.extractGetterMethod(this.managedType, dataMapping.EntityManagedField);
							//End of Getter And Setter Management
							
							//Check if the Field is a List or Collection Type.
							if (isCollection(dataMapping.EntityManagedType)) {
								dataMapping.IsCollection = true;
								
								//Get the generic type managed by the List.
								ParameterizedType listGenericType = (ParameterizedType)declaredField.getGenericType();
								Class<?> listGenericClass = (Class<?>)listGenericType.getActualTypeArguments()[0];
								if (listGenericClass != null) {
									dataMapping.EntityManagedType = listGenericClass;
								} else {
									dataMapping.EntityManagedType = Entity.class;
								}
							}
							
							//Check if the field is a ForeignKey field.
							if(dataMapping.ForeignKey) {
								//Format the special name of the Database table filed. 
								dataMapping.DataBaseFieldName = String.format(DataUtils.DATABASE_FK_FIELD_PATTERN, this.ownerEntityType.dataBaseTableName);
								dataMapping.DataBaseIsPrimaryKey = false;
								dataMapping.Unique = false;	
								dataMapping.IsSpecialField = true;
							}
							
							switch(dataMapping.DataBaseDataType) {
								case Entity.DATATYPE_ENTITY:						
									//Generate new ObjectSet for the inherited Entities Types.
									ObjectSet<Entity> entityObjectSet = generateNewInheritedObjectSet((ObjectSet<Entity>)this, dataMapping, false);
									dataMapping.DataBaseTableName = entityObjectSet.getDataBaseTableName();
									break;
								
								case Entity.DATATYPE_ENTITY_LINK:
									//Generate new ObjectSet for the inherited Entities Types.
									ObjectSet<Entity> linkedObjectSet = generateNewInheritedObjectSet(null, dataMapping, true);

									//By default set the field name same as managed type class name.
									String linkedTableName = dataMapping.EntityManagedType.getSimpleName();
									
									//Get Table Annotation from the ManagedType information.
									Table linkedTableAnnotation = (Table)dataMapping.EntityManagedType.getAnnotation(Table.class);
									if (linkedTableAnnotation != null) {
										if (linkedTableAnnotation.name().trim() != "") {
											linkedTableName = linkedTableAnnotation.name();
										}
									}
									
									dataMapping.DataBaseLinkedTableName = linkedTableName;
									dataMapping.DataBaseMiddleTableName = linkedObjectSet.getDatabaseLinkedTableName();
									dataMapping.DataBaseIsPrimaryKey = false;
									dataMapping.Unique = false;
									dataMapping.IsSpecialField = true;
									setContainsLinkedEntities(true);
									break;
									
								case Entity.DATATYPE_ENTITY_REFERENCE:
									//Generate new ObjectSet for the inherited Entities Types.
									ObjectSet<Entity> referenceObjectSet = generateNewInheritedObjectSet(null, dataMapping, false);
									
									//By default set the field name same as managed type class name.
									String tableName = dataMapping.EntityManagedType.getSimpleName();
									
									//Get Table Annotation from the ManagedType information.
									Table tableAnnotation = (Table)dataMapping.EntityManagedType.getAnnotation(Table.class);
									if (tableAnnotation != null) {
										if (tableAnnotation.name().trim() != "") {
											tableName = tableAnnotation.name();
										}
									}
									
									//Format the special name of the Database table filed. 
									if (dataMapping.DataBaseFieldName.equals("")) {
										dataMapping.DataBaseFieldName = String.format(DataUtils.DATABASE_FK_FIELD_PATTERN, tableName);
									}
									dataMapping.DataBaseTableName = referenceObjectSet.getDataBaseTableName();
									dataMapping.DataBaseIsPrimaryKey = false;
									dataMapping.Unique = false;
									dataMapping.IsSpecialField = true;
									break;
							}
							
							//If the DataBaseFieldName is empty, set the name same as Class field name.
							if (dataMapping.DataBaseFieldName == "") {
								dataMapping.DataBaseFieldName = dataMapping.EntityFieldName;
							}
							
							if (dataMapping.DataBaseFieldName == DataUtils.DATABASE_ID_FIELD_NAME) {
								dataMapping.IsSpecialField = true;
							}
							
							//Add the mapping to the data mappings collection.
							if (dataMapping.IsSpecialField) {
								pMappingsList.add(0, dataMapping);
							} else {
								//Extract table indexes definitions.
								extractDataIndexes(declaredField, dataMapping);
								
								pMappingsList.add(dataMapping);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
	
	/**
	 * Extract Data Index definition from the passes Field.
	 */
	@SuppressWarnings("deprecation")
	private void extractDataIndexes(Field pField, DataMapping pMapping) {
		
		if (getContext().getUseTableIndexes()) {
			if (pMapping.DataBaseDataType != Entity.DATATYPE_ENTITY
			    && pMapping.DataBaseDataType != Entity.DATATYPE_ENTITY_LINK
			    && pMapping.DataBaseDataType != Entity.DATATYPE_ENTITY_REFERENCE) {
	
				if (pField != null) {
					extractDataIndexConfiguration(pField.getAnnotation(TableIndex.class), pMapping);
					
					TableIndexes tableIndexesDefinition = pField.getAnnotation(TableIndexes.class);
					if (tableIndexesDefinition != null 
							&& tableIndexesDefinition.value() != null 
							&& tableIndexesDefinition.value().length > 0) {
						
						for(TableIndex tableIndexDefinition : tableIndexesDefinition.value()) {
							extractDataIndexConfiguration(tableIndexDefinition, pMapping);
						}
					}
				}
			}
		}
	}
	
	private void extractDataIndexConfiguration(TableIndex pTableIndex, DataMapping pMapping) {
		if (pTableIndex != null && pTableIndex.name() != null && !pTableIndex.name().trim().equals("")) {
			
			if (tableIndexes == null) {
				tableIndexes = new HashMap<String, List<DataIndex>>();
			}
			
			if (tableIndexes.containsKey(pTableIndex.name())) {
				boolean addField = true;
				for(DataIndex indexField : tableIndexes.get(pTableIndex.name())) {
					if (indexField.Name.equals(pMapping.DataBaseFieldName)) {
						addField = false;
						break;
					}
				}
				
				if (addField) {
					DataIndex indexConfiguration = new DataIndex();
					indexConfiguration.Name = pMapping.DataBaseFieldName;
					indexConfiguration.Direction =  pTableIndex.direction();
					
					tableIndexes.get(pTableIndex.name()).add(indexConfiguration);
				}
			} else {
				DataIndex indexConfiguration = new DataIndex();
				indexConfiguration.Name = pMapping.DataBaseFieldName;
				indexConfiguration.Direction =  pTableIndex.direction();
				
				List<DataIndex> databaseFields = new ArrayList<DataIndex>();
				databaseFields.add(indexConfiguration);
				tableIndexes.put(pTableIndex.name(), databaseFields);
			}
		}
	}
	
	/**
	 * Check if the type is a Collection. 
	 * @param pType Class to validate.
	 * @return True if is a Collection
	 */
	private Boolean isCollection(Class<?> pType) {
		Boolean returnedValue = false;
		
		if (pType == List.class) {
			returnedValue = true;
		}
		
		return returnedValue;
	}
	
	/**
	 * Generate new ObjectSet instance for any the Inherited Entities.
	 * @param managedType
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	private ObjectSet<Entity> generateNewInheritedObjectSet(ObjectSet<Entity> pOwnerSet, DataMapping pMapping, Boolean pLinked) throws AdaFrameworkException {
		//Generate new ObjectSet instance.
		ObjectSet<Entity> inheritedReferenceEntityObjectSet = new ObjectSet<Entity>(pOwnerSet, pMapping, getContext(), pLinked);
		inheritedReferenceEntityObjectSet.setDeleteOnCascade(this.isDeleteOnCascade());
		
		if (inheritedReferenceEntityObjectSet != null) {
			//Add the ObjectSet to the InheritedObjectSet internal list.
			inheritedObjectSets.add(inheritedReferenceEntityObjectSet);
		}
		
		return inheritedReferenceEntityObjectSet;
	}
	
	/**
	 * Load the DataBase fields mapping.
	 */
	@SuppressWarnings("deprecation")
	private String[] loadDataBaseTableFields() {
		List<String> returnedValue = new ArrayList<String>();
		
		for(DataMapping mapping : this.dataMappings) {
			if (!mapping.virtual) {
				switch (mapping.DataBaseDataType) {
					case Entity.DATATYPE_BOOLEAN:
					case Entity.DATATYPE_DATE:
					case Entity.DATATYPE_DATE_BINARY:
					case Entity.DATATYPE_INTEGER:
					case Entity.DATATYPE_LONG:
					case Entity.DATATYPE_DOUBLE:
					case Entity.DATATYPE_REAL:
					case Entity.DATATYPE_TEXT:
					case Entity.DATATYPE_GEOLOCATION:
					case Entity.DATATYPE_STRING:
					case Entity.DATATYPE_BLOB:
					case Entity.DATATYPE_ENTITY_REFERENCE:
						if (mapping.Unique) {
							if (dataBaseUniqueTableFields != "") {
								dataBaseUniqueTableFields += ", ";
							}
							dataBaseUniqueTableFields += mapping.DataBaseFieldName;
						}
						
						returnedValue.add(mapping.DataBaseFieldName);
						break;
				}
			}
		}
		
		return returnedValue.toArray(new String[returnedValue.size()]);
	}
	
	private String generateMiddleTableScript(DataMapping pMapping) throws AdaFrameworkException {
		String childTableName = pMapping.DataBaseTableName;
		String masterTableName = pMapping.DataBaseLinkedTableName;
		String tableName = pMapping.DataBaseMiddleTableName;
		
		String tableFieldsScript = String.format("%s_%s INTEGER NOT NULL, %s_%s INTEGER NOT NULL", childTableName, DataUtils.DATABASE_ID_FIELD_NAME, masterTableName, DataUtils.DATABASE_ID_FIELD_NAME) ;
		       tableFieldsScript += String.format(", PRIMARY KEY (%s_%s, %s_%s)", childTableName, DataUtils.DATABASE_ID_FIELD_NAME, masterTableName, DataUtils.DATABASE_ID_FIELD_NAME);
		String tableUniqueFieldsScript = "";
		String tableForeignKeyScript = "";
		       tableForeignKeyScript += String.format(DataUtils.DATABASE_TABLE_FOREIGN_KEY_PATTERN, String.format("%s_%s", childTableName, DataUtils.DATABASE_ID_FIELD_NAME), childTableName);
		       tableForeignKeyScript += String.format(DataUtils.DATABASE_TABLE_FOREIGN_KEY_PATTERN, String.format("%s_%s", masterTableName, DataUtils.DATABASE_ID_FIELD_NAME), masterTableName);
		
		return String.format(DataUtils.DATABASE_TABLE_PATTERN, tableName, tableFieldsScript, tableForeignKeyScript, tableUniqueFieldsScript).replace("  ", " ");
	}
	
	/**
	 * Generate the DataBase table script.
	 * @return Script with the table creation.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	@SuppressWarnings("deprecation")
	private String[] generateDataBaseTableScript(List<DataMapping> pDataMappings) throws AdaFrameworkException {
		String returnedValue = "";
		String tableName = "";
		String tableFieldsScript = "";
		String tableUniqueFieldsScript = "";
		String tableForeignKeyScript = "";
		
		List<String> tableScripts = new ArrayList<String>();
		
		for(DataMapping mapping : pDataMappings) {
			if (mapping.virtual) {
				Log.d(DataUtils.DEFAULT_LOGS_TAG, String.format("The field '%s' has been omitted its virtual condition.", mapping.EntityFieldName));
			} else {
				if ((mapping.DataBaseDataType != Entity.DATATYPE_ENTITY)) {
					
					tableName = mapping.DataBaseTableName;
					String dataFieldName = mapping.DataBaseFieldName;
					String dataTypeScript = "";
					String dataPrimaryKeyScript = "";
					String dataAllowNullsScript = "";
					
					switch(mapping.DataBaseDataType) {
						case Entity.DATATYPE_BOOLEAN:
						case Entity.DATATYPE_INTEGER:
						case Entity.DATATYPE_LONG:
						case Entity.DATATYPE_DOUBLE:
						case Entity.DATATYPE_DATE_BINARY:
						case Entity.DATATYPE_ENTITY_REFERENCE:
							dataTypeScript = "INT";
							break;
						case Entity.DATATYPE_DATE:
						case Entity.DATATYPE_TEXT:
						case Entity.DATATYPE_STRING:
						case Entity.DATATYPE_GEOLOCATION:
							dataTypeScript = "TEXT";
							break;
						case Entity.DATATYPE_REAL:
							dataTypeScript = "REAL";
							break;
						case Entity.DATATYPE_BLOB:
							dataTypeScript = "BLOB";
							break;
						case Entity.DATATYPE_ENTITY_LINK:
							tableScripts.add(generateMiddleTableScript(mapping));
							break;
					}
					
					if (mapping.DataBaseDataType != Entity.DATATYPE_ENTITY_LINK) {
						if (tableFieldsScript != "") {
							tableFieldsScript += ", ";
						}
						if (mapping.DataBaseIsPrimaryKey) {
							dataPrimaryKeyScript = "PRIMARY KEY ";	
						}
						if (!mapping.DataBaseAllowNulls) {
							dataAllowNullsScript = "NOT NULL ";
						}
						
						if (mapping.DataBaseFieldName == DataUtils.DATABASE_ID_FIELD_NAME) {
							dataTypeScript = "INTEGER";
							dataPrimaryKeyScript = "PRIMARY KEY AUTOINCREMENT";	
							dataAllowNullsScript = "NOT NULL";
						}
						
						//Format String
						tableFieldsScript += String.format("%s %s %s %s", 
								dataFieldName,
								dataTypeScript,
								dataPrimaryKeyScript,
								dataAllowNullsScript);
						
						//Clean up the excess space.
						tableFieldsScript = tableFieldsScript.replace("  ", " ");
						
						if (mapping.ForeignKey) {	
							if (this.ownerEntityType != null) {
								tableForeignKeyScript += String.format(DataUtils.DATABASE_TABLE_FOREIGN_KEY_PATTERN, mapping.DataBaseFieldName, this.ownerEntityType.dataBaseTableName);
							}
						} else {
							if (mapping.DataBaseDataType == Entity.DATATYPE_ENTITY_REFERENCE) {
								try {
									ObjectSet<Entity> ownerSet = getInheritedObjectSet(mapping.EntityManagedType, mapping);
									
									if (ownerSet != null) {
										tableForeignKeyScript += String.format(DataUtils.DATABASE_TABLE_FOREIGN_KEY_PATTERN, mapping.DataBaseFieldName, ownerSet.getDataBaseTableName());
									}
								} catch (Exception e) {
								}
							}
						}
					}
				}
			}
		}
		
		if (dataBaseUniqueTableFields != "") {
			tableUniqueFieldsScript = String.format(DataUtils.DATABASE_TABLE_UNIQUE_PATTERN, dataBaseUniqueTableFields);
		}
		
		returnedValue = String.format(DataUtils.DATABASE_TABLE_PATTERN, tableName, tableFieldsScript, tableForeignKeyScript, tableUniqueFieldsScript);
		tableScripts.add(0, returnedValue.replace("  ", " "));
		
		return tableScripts.toArray(new String[tableScripts.size()]);
	}
	
	/**
	 * Generate Database Index Scripts.
	 * @param pIndexes
	 * @return Database Index Scripts
	 */
	private String[] generateDataBaseTableIndexScript(HashMap<String, List<DataIndex>> pIndexes) {
		List<String> tableScripts = new ArrayList<String>();
		
		String indexFields = "";
		if (pIndexes != null && pIndexes.size() > 0) {
			for(String indexName : pIndexes.keySet()) {
				
				indexFields = "";
				for(DataIndex field : pIndexes.get(indexName)) {
					if (!field.Name.equals("")) {
						if (!indexFields.equals("")) {
							indexFields += ", ";
						}
					
						indexFields += field.Name;
						if (field.Direction == Entity.INDEX_DIRECTION_ASC)  {
							indexFields += " ASC";
						} else if (field.Direction == Entity.INDEX_DIRECTION_DESC)  {
							indexFields += " DESC";
						}
					}
				}
				
				indexName = indexName.replace("%TABLE_NAME%", dataBaseTableName);
				tableScripts.add(String.format(DataUtils.DATABASE_TABLE_INDEX_PATTERN, indexName, this.dataBaseTableName, indexFields));
			}
		}
		
		return tableScripts.toArray(new String[tableScripts.size()]);
	}
	
	private List<String> generateDataBaseTableIndexesScript(List<DataMapping> pDataMappings) {
		List<String> returnedValue = new ArrayList<String>();
		String tableName = "";
		String dataFieldName = "";
		
		if (this.dataBaseUseIndexes) {
			for(DataMapping mapping : pDataMappings) {
				if (mapping.DataBaseDataType != Entity.DATATYPE_ENTITY && mapping.DataBaseDataType != Entity.DATATYPE_ENTITY_LINK) {
					tableName = mapping.DataBaseTableName;
					dataFieldName = mapping.DataBaseFieldName;
					
					if (mapping.ForeignKey) {	
						if (mapping.virtual) {
							Log.d(DataUtils.DEFAULT_LOGS_TAG, String.format("The field '%s' has been omitted its virtual condition.", mapping.EntityFieldName));
						} else {
							String indexName = 	String.format(DataUtils.DATABASE_INDEX_FIELD_PATTERN, tableName, dataFieldName);
							String createIndexScript = String.format(DataUtils.DATABASE_TABLE_INDEX_PATTERN, indexName, tableName, dataFieldName);
							
							returnedValue.add(createIndexScript);
						}
					}
				}
			}
		}

		return returnedValue;
	}

	/**
	 * This method retrieve and generate the linked values.
	 * @param database
	 * @param pMapping
	 * @param pEntity
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	private void generateLinkedEntities(SQLiteDatabase pDatabase, DataMapping pMapping, Entity pEntity) throws AdaFrameworkException {
		SQLiteDatabase database = pDatabase;
		
		if (pMapping.DataBaseDataType == Entity.DATATYPE_ENTITY_LINK) {
			String columnName = String.format("%s_%s", pMapping.DataBaseLinkedTableName, DataUtils.DATABASE_ID_FIELD_NAME);
			Cursor linkedCursor = getContext().executeQuery(database, true, pMapping.DataBaseMiddleTableName, new String[] { columnName }, String.format("%s_%s = ?", pMapping.DataBaseTableName, DataUtils.DATABASE_ID_FIELD_NAME), new String[] { Float.toString(pEntity.getID()) }, null, null, String.format("%s_%s ASC", pMapping.DataBaseLinkedTableName, DataUtils.DATABASE_ID_FIELD_NAME), null);
			
			if (linkedCursor != null) {
				linkedCursor.moveToLast();
				linkedCursor.moveToFirst();
				
				if (linkedCursor.getCount() > 0) {
					ObjectSet<Entity> linkedObjectSet = getLinkedObjectSet(pMapping.EntityManagedType, pMapping);
					if (linkedObjectSet != null) {
						
						if (!pMapping.IsCollection) {
							Long elementID = linkedCursor.getLong(linkedCursor.getColumnIndex(columnName));
							Entity linkedEntity = linkedObjectSet.getElementByID(database, elementID);
							if (linkedEntity != null) {
								setEntityPropertyValue(pEntity, linkedEntity, pMapping);
							}
						} else {
							List<Entity> linckedValue = new ArrayList<Entity>();
							do{
								Long elementID = linkedCursor.getLong(linkedCursor.getColumnIndex(columnName));
								if (elementID != null) {
									 Entity linkedEntity = linkedObjectSet.getElementByID(database, elementID);
									 if (linkedEntity != null) {
										 linckedValue.add(linkedEntity);
									 }
								}
							} while(linkedCursor.moveToNext());
							
							setEntityPropertyValue(pEntity, linckedValue, pMapping);
						}
					}
				}
				
			}
			
			if (linkedCursor != null) {
				if (!linkedCursor.isClosed()) {
					linkedCursor.close();
				}
				linkedCursor = null;
			}
		}
		
	}
	
	/**
	 * Generate and Fill Entity instance.
	 * @param pCursor
	 * @return
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	@SuppressWarnings("deprecation")
	private Entity generateNewEntity(SQLiteDatabase database, Cursor pCursor, Entity pParent) throws AdaFrameworkException {
		Entity entity = null;
		try {
		
			entity = (Entity) this.managedType.newInstance();
			entity.setParent(pParent);
			
			for (DataMapping mapping : this.dataMappings) {
				if ((mapping.DataBaseDataType != Entity.DATATYPE_ENTITY) 
					&& (mapping.DataBaseDataType != Entity.DATATYPE_ENTITY_LINK)
					&& (!mapping.ForeignKey)) {
					
					int columnIndex = pCursor.getColumnIndex(mapping.DataBaseFieldName);
					
					if (columnIndex >= 0) {
						Object value = null;
						
						switch (mapping.DataBaseDataType) {
							case Entity.DATATYPE_BOOLEAN:
								value = pCursor.getInt(columnIndex);
								if ((Integer)value == DataUtils.DATABASE_BOOLEAN_VALUE_TRUE) {
									value = true;
								} else {
									value = false;
								}
								break;
							case Entity.DATATYPE_GEOLOCATION:
								value = pCursor.getString(columnIndex);
								if (mapping.Encrypted){
									value = EncryptionHelper.decrypt(this.dataContext.getMasterEncryptionKey(), (String)value);
								}
								value = deserializeGeolocation((String)value);
								break;
							case Entity.DATATYPE_TEXT:
							case Entity.DATATYPE_STRING:
								value = pCursor.getString(columnIndex);
								if (mapping.Encrypted){
									value = EncryptionHelper.decrypt(this.dataContext.getMasterEncryptionKey(), (String)value);
								}
								break;
							case Entity.DATATYPE_DATE_BINARY:
								Long timeStamp = pCursor.getLong(columnIndex);
								if (timeStamp != Long.MIN_VALUE) {
									value = new Date(timeStamp);
								} else {
									value = null;
								}
								break;
							case Entity.DATATYPE_DATE:
								value = pCursor.getString(columnIndex);
								if (mapping.Encrypted){
									value = EncryptionHelper.decrypt(this.dataContext.getMasterEncryptionKey(), (String)value);
								}
								value = getContext().StringToDate((String)value);
								break;
							case Entity.DATATYPE_REAL:
								value = pCursor.getFloat(columnIndex);
								break;
							case Entity.DATATYPE_INTEGER:
								value = pCursor.getInt(columnIndex);
								break;
							case Entity.DATATYPE_LONG:
								value = pCursor.getLong(columnIndex);
								break;
							case Entity.DATATYPE_DOUBLE:
								value = pCursor.getDouble(columnIndex);
								break;
							case Entity.DATATYPE_BLOB:
								value = pCursor.getBlob(columnIndex);
								
								if (value != null) {
									if (mapping.EntityManagedType == Bitmap.class) {
										value = BitmapFactory.decodeByteArray((byte[])value, 0, ((byte[])value).length);
									}
								}
								break;
							case Entity.DATATYPE_ENTITY_REFERENCE:
								Long foreignID = pCursor.getLong(columnIndex);
								
								if (foreignID != null) {
									ObjectSet<Entity> inheritedObjectSet = getInheritedObjectSet(mapping.EntityManagedType, mapping);
									if (inheritedObjectSet != null) {
										value = inheritedObjectSet.getElementByID(database, foreignID, entity);
									}
								}
								break;
						}
						
						if (value != null) {
							if (mapping.DataBaseFieldName == DataUtils.DATABASE_ID_FIELD_NAME) {
								entity.ID = (Long)value;
							} else {
								setEntityPropertyValue(entity, value, mapping);
							}	
						}
					}
					
				//Manage the linked dataMappings.
				} else if (mapping.DataBaseDataType == Entity.DATATYPE_ENTITY_LINK) {
					generateLinkedEntities(database, mapping, entity);
				}
			}
			
			if (entity != null) {
				if (ContainInheritedEntities()) {
					String wherePattern = String.format(DataUtils.DATABASE_FK_FIELD_PATTERN, this.getDataBaseTableName()) + " = ?";
					String[] whereValue = new String[] { Long.toString(entity.ID) };
					
					
					for (DataMapping mapping : this.dataMappings) {
						switch(mapping.DataBaseDataType) {
							case Entity.DATATYPE_ENTITY:
								//Get the InheritedObjectSet responsible for managing the type.
								ObjectSet<Entity> inheritedObjectSet = getInheritedObjectSet(mapping.EntityManagedType, mapping);
								
								if (inheritedObjectSet != null) {
									if (!mapping.IsCollection) {
										//If the managed field is not a List, set the first item of the inherited ObjectSet.
										inheritedObjectSet.fill(database, wherePattern, whereValue, DataUtils.DATABASE_ID_FIELD_NAME + " ASC", 1, entity);
										if(inheritedObjectSet.size() > 0) {
											setEntityPropertyValue(entity, inheritedObjectSet.getElementByID(database, inheritedObjectSet.get(database, 0, false).getID(), entity) , mapping);
										}
									} else {
										//If the managed field is a List, set the all items of the inherited ObjectSet.
										inheritedObjectSet.fill(database, wherePattern, whereValue, DataUtils.DATABASE_ID_FIELD_NAME + " ASC", entity);
										setEntityPropertyValue(entity, inheritedObjectSet, mapping);
									}
								}
								
								break;
						}
					}
				}
			}
			
			entity.setStatus(Entity.STATUS_NOTHING);
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	
		
		return entity;
	}
	
	/**
	 * Save inherited entities into DataBase.
	 * @param pDataBase
	 * @param pEntity
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	@SuppressWarnings("unchecked")
	private void saveInheritedEntities(SQLiteDatabase pDataBase, Entity pEntity) throws AdaFrameworkException {
		try {
			if (ContainInheritedEntities()) {
				for (DataMapping mapping : this.dataMappings) {
					switch(mapping.DataBaseDataType) {
						case Entity.DATATYPE_ENTITY:
							//Recovery the Entity ObjectSet Controller.
							ObjectSet<Entity> inheritedObjectSet = getInheritedObjectSet(mapping.EntityManagedType, mapping);
							
							if (inheritedObjectSet != null) {
								if (!mapping.IsCollection) {
									
									Entity inheritedEntity = null;
									if (mapping.getterMethod != null) {
										try {
											inheritedEntity = (Entity)mapping.getterMethod.invoke(pEntity, (Object[])null);
										} catch (Exception e) {
											throw new InaccessibleFieldException(pEntity.getClass().getName(), mapping.EntityFieldName, mapping.getterMethod.getName());
										}
									} else {
										inheritedEntity = (Entity)mapping.EntityManagedField.get(pEntity);
										
									}
								
									if (inheritedEntity != null) {
										inheritedObjectSet.save(pDataBase, inheritedEntity, pEntity.ID);
									}
								} else {
									List<Entity> inheritedEntities = null;
									if (mapping.getterMethod != null) {
										try {
											inheritedEntities = (List<Entity>)mapping.getterMethod.invoke(pEntity, (Object[])null);
										} catch (Exception e) {
											throw new InaccessibleFieldException(pEntity.getClass().getName(), mapping.EntityFieldName, mapping.getterMethod.getName());
										}
									} else {
										inheritedEntities = (List<Entity>)mapping.EntityManagedField.get(pEntity);
									}
									
									if (inheritedEntities != null) {
										if (inheritedEntities.size() > 0) {
											for(Entity inheritedEntity : inheritedEntities) {
												inheritedObjectSet.save(pDataBase, inheritedEntity, pEntity.ID);
											}
										}
									}
								}
							}
	
							break;
					}
				}
			}
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
	
	/**
	 * Delete Inherited elements from the DataBase.
	 * @param pDataBase
	 * @param pEntity
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void deleteInheritedEntities(SQLiteDatabase pDataBase, Entity pEntity) throws AdaFrameworkException {
		try {
			for (DataMapping mapping : this.dataMappings) {
				switch(mapping.DataBaseDataType) {
					case Entity.DATATYPE_ENTITY:
						//Create new ObjectSet the Entity type ObjectSet Controller.
						ObjectSet<Entity> inheritedObjectSet = getInheritedObjectSet(mapping.EntityManagedType, mapping);
						
						if (inheritedObjectSet != null) {
							if (!mapping.IsCollection) {
								Entity inheritedEntity = (Entity)getEntityPropertyValue(pEntity, mapping);
							
								inheritedEntity.setStatus(Entity.STATUS_DELETED);
								inheritedObjectSet.save(pDataBase, inheritedEntity, pEntity.ID);
							} else {
								List<Entity> inheritedEntities = (List<Entity>)getEntityPropertyValue(pEntity, mapping);
								
								if (inheritedEntities != null) {
									if (inheritedEntities.size() > 0) {
										for(Entity inheritedEntity : inheritedEntities) {
											inheritedEntity.setStatus(Entity.STATUS_DELETED);
											inheritedObjectSet.add(inheritedEntity);
										}
										inheritedObjectSet.save(pDataBase, pEntity.ID);
									}
								}
							}
						}
						break;
				}
			}
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
	
	/**
	 * Get the value of property into the Entity.
	 * @param pEntity
	 * @param pMapping
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private Object getEntityPropertyValue(Entity pEntity, DataMapping pMapping) throws InaccessibleFieldException {
		Object returnedValue = null;
		
		try {
			
			if (pMapping.getterMethod != null) {
				returnedValue = pMapping.getterMethod.invoke(pEntity, (Object[])null);
			} else {
				returnedValue = pMapping.EntityManagedField.get(pEntity);
			}
			
		} catch (Exception e) {
			if (pMapping.getterMethod != null) {
				throw new InaccessibleFieldException(pEntity.getClass().getName(), pMapping.EntityManagedField.getName(), pMapping.getterMethod.getName());
			} else {
				throw new InaccessibleFieldException(pEntity.getClass().getName(), pMapping.EntityManagedField.getName(), "");
			}
		}
		
		return returnedValue;
	}
	
	/**
	 * Set the value of property into the Entity.
	 * @param pEntity
	 * @param pValue
	 * @param pMapping
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws java.lang.reflect.InvocationTargetException
	 */
	private void setEntityPropertyValue(Entity pEntity, Object pValue, DataMapping pMapping) throws InaccessibleFieldException {
		try {
			
			if (pMapping.setterMethod != null) {
				pMapping.setterMethod.invoke(pEntity, pValue);
			} else {
				pMapping.EntityManagedField.set(pEntity, pValue);
			}
			
		} catch (Exception e) {
			if (pMapping.setterMethod != null) {
				throw new InaccessibleFieldException(pEntity.getClass().getName(), pMapping.EntityManagedField.getName(), pMapping.setterMethod.getName());
			} else {
				throw new InaccessibleFieldException(pEntity.getClass().getName(), pMapping.EntityManagedField.getName(), "");
			}
		}
	}
	
	/**
	 * This method generate DataBase Actions content values. Used for INSERT and UPDATES. 
	 * @return
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	@SuppressWarnings("deprecation")
	private ContentValues generateContentValues(Entity pEntity, Long pOwnerID) throws AdaFrameworkException {
		ContentValues values = new ContentValues();
		
		try {
			for (DataMapping mapping : this.dataMappings) {
				if (mapping.virtual) {
					Log.d(DataUtils.DEFAULT_LOGS_TAG, String.format("The field '%s' has been omitted its virtual condition.", mapping.EntityFieldName));
				} else {
					//Skip Entities and Entities Collection because this objects don't allow storage in the object table.
					if (mapping.DataBaseDataType != Entity.DATATYPE_ENTITY && mapping.DataBaseDataType != Entity.DATATYPE_ENTITY_LINK) {
						if (mapping.DataBaseFieldName != DataUtils.DATABASE_ID_FIELD_NAME) {
							Object propertyValue = getEntityPropertyValue(pEntity, mapping);
							
							if (mapping.ForeignKey) {
								if (pOwnerID != null) {
									values.put(mapping.DataBaseFieldName, pOwnerID);
								} else {
									values.putNull(mapping.DataBaseFieldName);
								}
							} else {
								if (propertyValue != null) {
									switch (mapping.DataBaseDataType) {
										case Entity.DATATYPE_INTEGER:
											values.put(mapping.DataBaseFieldName, (Integer)propertyValue);
											break;
										case Entity.DATATYPE_LONG:
											values.put(mapping.DataBaseFieldName, (Long)propertyValue);
											break;
										case Entity.DATATYPE_DOUBLE:
											values.put(mapping.DataBaseFieldName, (Double)propertyValue);
											break;
										case Entity.DATATYPE_BOOLEAN:
											if ((Boolean)propertyValue) {
												values.put(mapping.DataBaseFieldName, DataUtils.DATABASE_BOOLEAN_VALUE_TRUE);
											} else {
												values.put(mapping.DataBaseFieldName, DataUtils.DATABASE_BOOLEAN_VALUE_FALSE);
											}
											break;
										case Entity.DATATYPE_REAL:
											values.put(mapping.DataBaseFieldName, (Float)propertyValue);
											break;
										case Entity.DATATYPE_GEOLOCATION:
											if (propertyValue instanceof Location) {
												String geolocationValue = serializeGeolocation((Location)propertyValue);
												
												if (geolocationValue != null) {
													if (mapping.Encrypted){
														geolocationValue = EncryptionHelper.encrypt(this.dataContext.getMasterEncryptionKey(), geolocationValue);
													}
													
													values.put(mapping.DataBaseFieldName, geolocationValue);
												} else {
													values.putNull(mapping.DataBaseFieldName);
												}
											} else {
												values.putNull(mapping.DataBaseFieldName);
											}
											
											break;
										case Entity.DATATYPE_TEXT:
										case Entity.DATATYPE_STRING:
										case Entity.DATATYPE_DATE:
											values.put(mapping.DataBaseFieldName, getContext().prepareObjectToDatabase(propertyValue, mapping));
											break;
										case Entity.DATATYPE_DATE_BINARY:
											if (propertyValue instanceof Date) {
												values.put(mapping.DataBaseFieldName, ((Date)propertyValue).getTime());
											} else {
												values.put(mapping.DataBaseFieldName, Long.MIN_VALUE);
											}
											break;
										case Entity.DATATYPE_BLOB:
											if (propertyValue instanceof Bitmap) {
												byte[] byteArrayValue = extractBitmatBytes((Bitmap)propertyValue, mapping.BitmapCompression);
												
												if (byteArrayValue != null){
													values.put(mapping.DataBaseFieldName, byteArrayValue);
												} else {
													values.putNull(mapping.DataBaseFieldName);
												}
											} else {
												if (propertyValue instanceof byte[]) {
													values.put(mapping.DataBaseFieldName, (byte[])propertyValue);
												} else {
													values.putNull(mapping.DataBaseFieldName);
												}
											}
											break;
										case Entity.DATATYPE_ENTITY_REFERENCE:
											propertyValue = ((Entity)propertyValue).getID();
											values.put(mapping.DataBaseFieldName, (Long)propertyValue);
											break;
									}
								} else {
									if (mapping.DataBaseDataType == Entity.DATATYPE_DATE_BINARY) {
										values.put(mapping.DataBaseFieldName, Long.MIN_VALUE);
									} else {
										values.putNull(mapping.DataBaseFieldName);
									}
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
		
		return values;
	}
	
	/**
	 * This method compress and stract bytes from Bitmap.
	 * @param pBitmap
	 * @return
	 * @throws java.io.IOException
	 */
	private byte[] extractBitmatBytes(Bitmap pBitmap, CompressFormat pFormat) {
		byte[] returnedValue = null;
		
		try{
			
			if (pBitmap != null){
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				pBitmap.compress(pFormat, 100, out);
		        
		        returnedValue = out.toByteArray();
		        
		        out.close();
		        out = null;
			}
			
		} catch (Exception e) {
			returnedValue = null;
		}
		
		return returnedValue;
	}
	
	/**
	 * Save new entity into DataBase.
	 * @param pDatabase
	 * @param pEntity
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	private void saveNewEntity(SQLiteDatabase pDatabase, Entity pEntity, Long pOwnerID) throws AdaFrameworkException {
		SQLiteDatabase database = pDatabase;
		
		try {
			
			if (database != null) {
				if (pEntity != null) {
					if (pEntity.getID() == null) {
						
							ContentValues insertValues = generateContentValues(pEntity, pOwnerID);
							Long entityID = getContext().executeInsert(database, this.dataBaseTableName, null, insertValues);
							
							pEntity.setID(entityID);
							pEntity.setStatus(Entity.STATUS_NOTHING);
						
					} else {
						saveUpdatedEntity(pDatabase, pEntity, pOwnerID);
					}
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
	
	/**
	 * Update entity information into DataBase.
	 * @param pDatabase
	 * @param pEntity
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	private void saveUpdatedEntity(SQLiteDatabase pDatabase, Entity pEntity, Long pOwnerID) throws AdaFrameworkException {
		SQLiteDatabase database = pDatabase;
		
		try {
			
			if (database != null) {
				if (pEntity != null) {
					if (pEntity.getID() != null) {

						ContentValues updateValues = generateContentValues(pEntity, pOwnerID);						
						getContext().executeUpdate(database, this.dataBaseTableName, updateValues, DataUtils.DATABASE_ID_FIELD_WHERE, new String[] { Long.toString(pEntity.getID()) });
						
						pEntity.setStatus(Entity.STATUS_NOTHING);
						
					} else {
						saveNewEntity(database, pEntity, pOwnerID);
					}
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
	
	/**
	 * Save linked entities.
	 * @param pDatabase
	 * @param pEntity
	 * @param pOwnerID
	 * @throws com.mobandme.ada.exceptions.InaccessibleFieldException
	 */
	@SuppressWarnings("unchecked")
	private void saveLinkedEntities(SQLiteDatabase pDatabase, Entity pEntity) throws InaccessibleFieldException {
		if (dataMappings != null && dataMappings.size() > 0) {
			for(DataMapping mapping : dataMappings) {
				if (mapping.DataBaseDataType == Entity.DATATYPE_ENTITY_LINK) {
					//Clear all previous values.
					if (pEntity.getID() != null) {
						getContext().executeDelete(pDatabase, mapping.DataBaseMiddleTableName, String.format("%s_%s = ?", mapping.DataBaseTableName, DataUtils.DATABASE_ID_FIELD_NAME), new String[] { Long.toString(pEntity.getID()) });
					}
					
					if (!mapping.IsCollection) {
						Entity inheritedEntity = (Entity)getEntityPropertyValue(pEntity, mapping);
						if (inheritedEntity != null) {
							ContentValues values = new ContentValues();
							values.put(String.format("%s_%s", mapping.DataBaseTableName, DataUtils.DATABASE_ID_FIELD_NAME), pEntity.getID());
							values.put(String.format("%s_%s", mapping.DataBaseLinkedTableName, DataUtils.DATABASE_ID_FIELD_NAME), inheritedEntity.getID());
							getContext().executeInsert(pDatabase, mapping.DataBaseMiddleTableName, null, values);
						}
					} else {
						List<Entity> inheritedEntities = (List<Entity>)getEntityPropertyValue(pEntity, mapping);
						if (inheritedEntities != null && inheritedEntities.size() > 0) {
							for(Entity linkedEntity : inheritedEntities) {
								ContentValues values = new ContentValues();
								values.put(String.format("%s_%s", mapping.DataBaseTableName, DataUtils.DATABASE_ID_FIELD_NAME), pEntity.getID());
								values.put(String.format("%s_%s", mapping.DataBaseLinkedTableName, DataUtils.DATABASE_ID_FIELD_NAME), linkedEntity.getID());
								getContext().executeInsert(pDatabase, mapping.DataBaseMiddleTableName, null, values);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Delete entity from DataBase.
	 * @param pDatabase
	 * @param pEntity
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	private void saveDeletedEntity(SQLiteDatabase pDatabase, final Entity pEntity, Long pOwnerID) throws AdaFrameworkException {
		try {
			if (pDatabase != null) {
				if (pEntity != null) {
					if (pEntity.getID() != null) {	
						if (isDeleteOnCascade()) {
							if (ContainInheritedEntities()) {
								deleteInheritedEntities(pDatabase, pEntity);
							}
						}
						
						//Delete entity from Database.
						getContext().executeDelete(pDatabase, this.dataBaseTableName, DataUtils.DATABASE_ID_FIELD_WHERE, new String[] { Long.toString(pEntity.getID()) });
						
						super.remove(pEntity);

						if (dataAdapter != null) {
							if (isContextActivity()) {
								((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
									@SuppressWarnings("unchecked")
									@Override
									public void run() {
										int position = dataAdapter.getPosition((T)pEntity);
										
										if (position >= 0){
											dataAdapter.remove((T)pEntity);
										}					
									}
								});
							}
						}
					}
				}
			}
		} catch (Exception e) {
			ExceptionsHelper.manageException(this, e);
		}
	}
		
	/**
	 * Delete linked entity from DataBase.
	 * @param pDatabase
	 * @param pEntity
	 */
	private void saveDeletedLinkedEntity(SQLiteDatabase pDatabase, final Entity pEntity) {
		if (dataMappings != null && dataMappings.size() > 0) {
			for(DataMapping mapping : dataMappings) {
				if (mapping.DataBaseDataType == Entity.DATATYPE_ENTITY_LINK) {
					//Clear all previous values.
					if (pEntity.getID() != null) {
						getContext().executeDelete(pDatabase, mapping.DataBaseMiddleTableName, String.format("%s_%s = ?", mapping.DataBaseTableName, DataUtils.DATABASE_ID_FIELD_NAME), new String[] { Long.toString(pEntity.getID()) });
					}
				}
			}
		}
	}
	
	/**
	 * Execute notifyDataSetChanged method of the ObjectSet ArrayAdapter.
	 */
	private void notifyDataSetChanged() {
		try {
			
			if (notifyAdapterChanges) {
				if (this.dataAdapter != null) {
					dataAdapter.notifyDataSetChanged();					
				}
			}
			
		} catch (Exception e) {
		}
	}
	
	@Override
	public T get(int index) {
		T returnedValue = null;
		
		try {
			
			returnedValue = this.get(null, index);
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(DataUtils.DEFAULT_LOGS_TAG, e.toString());
		}
		
		return returnedValue;
	}
	
	T get(SQLiteDatabase pDatabase, int pIndex) throws AdaFrameworkException {
		return this.get(pDatabase, pIndex, true);
	}
	
	T get(SQLiteDatabase pDatabase, int pIndex, boolean pForceLoad) throws AdaFrameworkException {
		T entity = super.get(pIndex);
		
		try {
			
			if (pForceLoad) {
				if (entity != null) {
					if (getContext().isLazyLoading() && !entity.getLazyLoaded()) {
						entity = getElementByID(pDatabase, entity.getID());
						super.set(pIndex, entity);
					}
				}
			}
		
		} catch (Exception e) {
			ExceptionsHelper.manageException(e);
		}
		
		return entity;
	}
	
	

	/**
	 * Add a new Entity into the ObjectSet.
	 */
	public boolean add(final T object) {
		boolean returnedValue = super.add(object);
		
		if (this.dataAdapter != null) {
			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dataAdapter.add(object);
						
						notifyDataSetChanged();
					}
				});
			}
		}
		
		return returnedValue;
	}
	
	/**
	 * Add a new Entity into the ObjectSet into specific position.
	 */
	public void add(final int location, final T object) {
		super.add(location, object);
		
		if (this.dataAdapter != null) {
			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dataAdapter.insert(object, location);
						
						notifyDataSetChanged();
					}
				});
			}
		}
	}
	
	@Override
	public boolean addAll(Collection<? extends T> collection) {
		boolean returnedValue = super.addAll(collection);
		
		if (this.dataAdapter != null) {
			for(T entity : collection) {
				this.add(entity);
			}

			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}
		
		return returnedValue;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> collection) {
		boolean returnedValue = super.addAll(index, collection);
	
		if (this.dataAdapter != null) {
			int location = index;
			for(T entity : collection) {
				this.add(location, entity);
				location++;
			}
			
			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}
		
		return returnedValue;
	}

	@Override
	public T remove(final int index) {
		this.get(index).setStatus(Entity.STATUS_DELETED);
		
		if (this.dataAdapter != null) {
			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dataAdapter.remove(get(index));
						
						notifyDataSetChanged();
					}
				});
			}
		}
		
		return this.get(index);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(final Object object) {
		((Entity)object).setStatus(Entity.STATUS_DELETED);
		
		if (this.dataAdapter != null) {
			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dataAdapter.remove((T)object);
						
						notifyDataSetChanged();
					}
				});
			}
		}

		return true;
	}
	
	/**
	 * This method remove all entities from the ObjectSet, use this method in combination with save method to validate the changes. 
	 */
	public void removeAll() {
		
		if (this.size() > 0) {
			for(Entity entity : this) {
				entity.setStatus(Entity.STATUS_DELETED);
			}
			
			if (this.dataAdapter != null) {
				if (isContextActivity()) {
					((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dataAdapter.clear();
							
							notifyDataSetChanged();
						}
					});
				}
			}
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		
		if (this.dataAdapter != null) {
			if (isContextActivity()) {
				((Activity)this.dataContext.getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dataAdapter.clear();
						
						notifyDataSetChanged();
					}
				});
			}
		}
	}
	
	/**
	 * This method check if the ObjectSet context is instance of Activity class.
	 * @return Return True if the ObjectSet context is instance of Activity class.
	 */
	private Boolean isContextActivity() {
		Boolean returnedValue = false;
		
		try {
			
			if (this.dataContext.getContext() != null) {
				if (this.dataContext.getContext() instanceof Activity) {
					returnedValue = true;
				}
			}
			
		} catch (Exception e) {
			returnedValue = false;
		}
		
		return returnedValue;
	}
	
	/**
	 * This method serialize the location object, preparing it to be saved into the database.  
	 * @param pLocation Location object.
	 * @return String with the object serialization.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	private String serializeGeolocation(Location pLocation) throws AdaFrameworkException {
		String returnedValue = null;
		
		try {
			
			if (pLocation != null) {
				returnedValue = "";
				returnedValue += pLocation.getProvider();
				returnedValue += ";";
				returnedValue += Double.toString(pLocation.getLatitude());
				returnedValue += ";";
				returnedValue += Double.toString(pLocation.getLongitude());
				returnedValue += ";";
				returnedValue += Float.toString(pLocation.getAccuracy());
				returnedValue += ";";
				returnedValue += Float.toString(pLocation.getBearing());
				returnedValue += ";";
				returnedValue += Float.toString(pLocation.getSpeed());
				returnedValue += ";";
				returnedValue += Double.toString(pLocation.getAltitude());
				returnedValue += ";";
				returnedValue += Long.toString(pLocation.getTime());
			}
			
		} catch (Exception e) {
			throw new AdaFrameworkException(e);
		}
		
		return returnedValue; 
	}
	
	/**
	 * This method deserialize the location object, preparing it to be loaded into the Entity property.  
	 * @param pLocation Location object.
	 * @return String with the object serialization.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	private Location deserializeGeolocation(String pLocation) throws AdaFrameworkException {
		Location returnedValue = null;
		
		try {
			
			if (pLocation != null && pLocation.trim() != "") {
				String[] locationValues = pLocation.split(";");
				
				if (locationValues.length == 8) {
					returnedValue = new Location(locationValues[0]);
					returnedValue.setLatitude(Double.valueOf(locationValues[1]));
					returnedValue.setLongitude(Double.valueOf(locationValues[2]));
					returnedValue.setAccuracy(Float.valueOf(locationValues[3]));
					returnedValue.setBearing(Float.valueOf(locationValues[4]));
					returnedValue.setSpeed(Float.valueOf(locationValues[5]));
					returnedValue.setAltitude(Float.valueOf(locationValues[6]));
					returnedValue.setTime(Long.valueOf(locationValues[7]));
				}
			}
			
		} catch (Exception e) {
			throw new AdaFrameworkException(e);
		}
		
		return returnedValue; 
	}
}
