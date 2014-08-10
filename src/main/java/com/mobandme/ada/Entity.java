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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.mobandme.ada.annotations.CustomValidation;
import com.mobandme.ada.annotations.Databinding;
import com.mobandme.ada.annotations.RangeValidation;
import com.mobandme.ada.annotations.RegularExpressionValidation;
import com.mobandme.ada.annotations.RequiredFieldValidation;
import com.mobandme.ada.annotations.TableField;
import com.mobandme.ada.exceptions.AdaFrameworkException;
import com.mobandme.ada.exceptions.InaccessibleFieldException;
import com.mobandme.ada.validators.ValidationResult;
import com.mobandme.ada.validators.Validator;

/**
 * Represents the smallest unit to be implemented by the Entities.
 * @version 2.4.3
 * @author Mob&Me
 */
public abstract class Entity {
	final static int DATATYPE_EMPTY = 0;
	
	/**
	 * Default value for Table Index Field direction.
	 */
	public final static int INDEX_DIRECTION_EMPTY = 0;
	
	/**
	 * Ascending value for Table Index Field direction.
	 */
	public final static int INDEX_DIRECTION_ASC = 1;
	
	/**
	 * Descending value for Table Index Field direction.
	 */
	public final static int INDEX_DIRECTION_DESC = 2;
	
	/**
	 * Use to represent Boolean value. Use this datatype with Boolean values.
	 */
	public final static int DATATYPE_BOOLEAN = 1;
	/**
	 * Use to represent Integer number value. Use this datatype with Integer values.
	 */
	public final static int DATATYPE_INTEGER = 2;
	/**
	 * Use to represent Long number value.  Use this datatype with Boolean values.
	 */
	public final static int DATATYPE_LONG = 3;
	/**
	 * Use to represent Long number value.  Use this datatype with Double values.
	 */
	public final static int DATATYPE_DOUBLE = 4;
	/**
	 * Use to represent Real/Decimal number value.  Use this datatype with Float values.
	 */
	public final static int DATATYPE_REAL = 5;
	/**
	 * Use to represent String value. Use this datatype with String values.
	 */
	public final static int DATATYPE_TEXT = 6;
	/**
	 * Use to represent String value. Use this datatype with String values.
	 */
	public final static int DATATYPE_STRING = 7;
	/**
	 * Use to represent DateTime value.  Use this datatype with Date values.
	 */
	@Deprecated()
	public final static int DATATYPE_DATE = 8;
	/**
	 * Use to represent DateTime value.  Use this datatype with Date values.
	 */
	public final static int DATATYPE_DATE_BINARY = 14;
	/**
	 * Use to represent Binary Field or Byte[].  Use this datatype with Bitmap or byte[] values.
	 */
	public final static int DATATYPE_BLOB = 9;
	/**
	 * Use to represent single Entity.  Use this datatype with Entity values.
	 */
	public final static int DATATYPE_ENTITY = 10;
	/**
	 * Use to represent external Entity reference.  Use this datatype with Entity class values.
	 * @deprecated Please use, DATATYPE_ENTITY_LINK data type.
	 */
	@Deprecated()
	public final static int DATATYPE_ENTITY_REFERENCE = 11;
	/**
	 * Use to represent Geolocation point or GPS Location, Use this datatype with Location types values.
	 */
	public final static int DATATYPE_GEOLOCATION = 12;
	/**
	 * Use to represent external Entity reference.  Use this datatype with Entity class values.
	 */
	public final static int DATATYPE_ENTITY_LINK = 13;
	
	/**
	 * Represents the state for entities that are not to be saved. 
	 * This is the default when loading entities from the database engine.
	 */
	public final static int STATUS_NOTHING = 0;
	/**
	 * Represents the state for new entities.
	 * This is the default when create new Instance of Entity class.
	 */
	public final static int STATUS_NEW = 1;
	/**
	 * Represents the state for entities that must be updated.
	 */
	public final static int STATUS_UPDATED = 2;
	/**
	 * Represents the state for entities that must be eliminated. 
	 */
	public final static int STATUS_DELETED = 3;
	
	/***
	 * Represents the state of the Entity. 
	 */
	private int status = Entity.STATUS_NEW;
	
	private transient List<DataBinding>      dataBinding = null;
	private transient List<Validation>       validations = null;
	private transient List<ValidationResult> validationResult = null;
	private Boolean                          lazyLoaded = true;
	
	/**
	 * This method retrieve of the Entity has been fully loaded.
	 * @return True or False.
	 */
	Boolean getLazyLoaded() { return lazyLoaded; }
	
	/**
	 * This method set if the Entity has been fully loaded.
	 * @param pLoaded
	 */
	void setLazyLoaded(Boolean pLoaded) { this.lazyLoaded = pLoaded; }
	
	/**
	 * This method returns the last validation process result.
	 * @return
	 */
	public List<ValidationResult> getValidationResult() {
		return this.validationResult;
	}
	
	/**
	 * This method returns the last validation process result.
	 * @return
	 */
	public String getValidationResultString() {
		return getValidationResultString("\r\n* ");
	}
	
	/**
	 * This method returns the last validation process result.
	 * @return
	 */
	public String getValidationResultString(String pSeparator) {
		String returnedValue = "";
		
		if (this.validationResult != null) {
			if (this.validationResult.size() > 0) {
				for(ValidationResult result : this.validationResult) {
					
					if (pSeparator != null) {
						returnedValue += String.format("%s%s", pSeparator, result.getMessage());
					} else {
						returnedValue += String.format("%s", result.getMessage());
					}
				}
			}
		}
		
		return returnedValue;
	}
	
	
	
	/**
	 * Returns the current status of the Entity.
	 */
	public int getStatus() { return status; }
	
	/**
	 * Set the state of the Entity.
	 * @param pStatus
	 */
	public void setStatus(Integer pStatus) { 
		if ((pStatus == STATUS_NOTHING) ||
			(pStatus == STATUS_NEW) ||
			(pStatus == STATUS_UPDATED) ||
			(pStatus == STATUS_DELETED)) {
			
			status = pStatus;
		} 
	}
	
	protected Entity parent = null;
	/**
	 * Obtain the immediately top level entity parent in the hierarchical structure.
	 * @return Returns a Parent entity instance or null if it is not exist.
	 */
	public Entity getParent() { return this.parent; }
	/**
	 * Establish the parent entity.
	 * @param pEntityParent Parent entity instance.
	 */
	void setParent(Entity pEntityParent) { this.parent = pEntityParent; }
	
	
	/***
	 * Unique identifier of the Entity in the Database.
	 */
	@TableField(name = "ID", datatype = Entity.DATATYPE_LONG, required = true)
	protected Long ID = null;
	
	/** 
	 * Get the unique Id of the Entity into Database.
	 * @return The unique identifier of the entity in the DataBase.
	 */
	public Long getID() { return this.ID; }
	
	/***
	 * Set the unique Id of the Entity into Database.
	 * @param pID
	 */
	void setID(Long  pID) { ID = pID; }
	
	private void loadDataBindings() throws AdaFrameworkException {
		try {
			
			dataBinding = new ArrayList<DataBinding>();
			Class<?> managedType = this.getClass();
			
			if (managedType != Entity.class) {
				while((managedType != Entity.class || managedType == Object.class)) {
					if (managedType != null) {
						extractDataBindings(managedType.getDeclaredFields());
					}
					
					managedType = managedType.getSuperclass(); 
					if (managedType == null) {
						break;
					}
				}
			}

			if (managedType != null) { 
				if (managedType == Entity.class) {
					extractDataBindings(managedType.getDeclaredFields());
				}
			}
			
			
		} catch (Exception e) {
			throw new AdaFrameworkException(e);
		}
	}
	
	private void extractDataBindings(Field[] pFields) {
		if (pFields != null) {
			if (pFields.length > 0) {
				for(Field field : pFields) {
					Databinding dataBindAnnotation = field.getAnnotation(Databinding.class);
					if (dataBindAnnotation != null) {
						DataBinding dataBinding = new DataBinding();
						dataBinding.EntityField = field;
						dataBinding.ViewId = dataBindAnnotation.ViewId();
						dataBinding.Binder = dataBindAnnotation.binder();
						dataBinding.Parser = dataBindAnnotation.parser();
						
						dataBinding.setterMethod = ReflectionHelper.extractSetterMethod(this.getClass(), field);
						dataBinding.getterMethod = ReflectionHelper.extractGetterMethod(this.getClass(), field);

						this.dataBinding.add(dataBinding);
					}
				}
			}
		}
	}
	
	private void loadValidations(Context pContext) {
		validations = new ArrayList<Validation>();
		Class<?> managedType = this.getClass();
		
		if (managedType != Entity.class) {
			while((managedType != Entity.class || managedType == Object.class)) {
				if (managedType != null) {
					extractValidations(pContext, managedType.getDeclaredFields());
				}
				
				managedType = managedType.getSuperclass(); 
				if (managedType == null) {
					break;
				}
			}
		}

		if (managedType != null) { 
			if (managedType == Entity.class) {
				extractValidations(pContext, managedType.getDeclaredFields());
			}
		}
	}
	
	private void extractValidations(Context pContext, Field[] pFields) {
		if (pFields != null) {
			if (pFields.length > 0) {
				for(Field field : pFields) {
					
					Annotation[] fieldAnnotations = field.getAnnotations();
					if (fieldAnnotations != null) {
						if (fieldAnnotations.length > 0) {
							for(Annotation annotation : fieldAnnotations) {
								
								Method getterMethod = ReflectionHelper.extractGetterMethod(this.getClass(), field);
								Validation validation = null;
								
								if (annotation instanceof RequiredFieldValidation) {
									validation = new Validation();
									validation.EntityField = field;
									validation.getterMethod = getterMethod;
									validation.Annotation = annotation;
											
									String message = "";
									if (pContext != null) {
										if (((RequiredFieldValidation)annotation).messageResourceId() != 0) {
											message = pContext.getString(((RequiredFieldValidation)annotation).messageResourceId());
										}
									}
									if ((message == null) || (message.trim().equals(""))) {
										message = ((RequiredFieldValidation)annotation).message();
									}
									
									validation.message = message;
									validation.Validator = ((RequiredFieldValidation)annotation).validator();
								} else if (annotation instanceof RegularExpressionValidation) {
									validation = new Validation();
									validation.EntityField = field;
									validation.getterMethod = getterMethod;
									validation.Annotation = annotation;
									
									String message = "";
									if (pContext != null) {
										if (((RegularExpressionValidation)annotation).messageResourceId() != 0) {
											message = pContext.getString(((RegularExpressionValidation)annotation).messageResourceId());
										}
									}
									
									if ((message == null) || (message.trim().equals(""))) {
										message = ((RegularExpressionValidation)annotation).message();
									}
									validation.message = message;
									validation.Validator = ((RegularExpressionValidation)annotation).validator();
								} else if (annotation instanceof RangeValidation) {
									validation = new Validation();
									validation.EntityField = field;
									validation.getterMethod = getterMethod;
									validation.Annotation = annotation;
									
									String message = "";
									if (pContext != null) {
										if (((RangeValidation)annotation).messageResourceId() != 0) {
											message = pContext.getString(((RangeValidation)annotation).messageResourceId());
										}
									}
									
									if ((message == null) || (message.trim().equals(""))) {
										message = ((RangeValidation)annotation).message();
									}
									
									validation.message = message;
									validation.Validator = ((RangeValidation)annotation).validator();
								} else if (annotation instanceof CustomValidation) {
									validation = new Validation();
									validation.EntityField = field;
									validation.getterMethod = getterMethod;
									validation.Annotation = annotation;
									
									String message = "";
									if (pContext != null) {
										if (((CustomValidation)annotation).messageResourceId() != 0) {
											message = pContext.getString(((CustomValidation)annotation).messageResourceId());
										}
									}
									
									if ((message == null) || (message.trim().equals(""))) {
										message = ((CustomValidation)annotation).message();
									}
									validation.message = message;
									validation.Validator = ((CustomValidation)annotation).validator();
								}
								
								if (validation != null) {
									this.validations.add(validation);
								}
							}
						}
					}
				}
			}
		}
	}
	

	/**
	 * This method execute the databinding ralations. 
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public void bind(Activity pActivity) throws AdaFrameworkException {
		bind(pActivity, DataBinder.BINDING_ENTITY_TO_UI);
	}
	
	/**
	 * This method execute the databinding ralations. 
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public void bind(Activity pActivity, int pDirection) throws AdaFrameworkException {
		try {
			
			if (pActivity != null) {
				if (this.dataBinding == null) {
					loadDataBindings();
				}
				
				if (this.dataBinding != null) {
					if (this.dataBinding.size() > 0) {
						for(DataBinding binding : this.dataBinding) {
							View view = pActivity.findViewById(binding.ViewId);
							
							if (binding.Binder != null) {
								Object binder = binding.Binder.newInstance();
								if (binder instanceof DataBinder) {
									((DataBinder)binder).bind(binding, this, view, pDirection);
								}
							}
						}
						
						if (pDirection == DataBinder.BINDING_UI_TO_ENTITY) {
							if (this.getStatus() == STATUS_NOTHING) {
								this.setStatus(STATUS_UPDATED);
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			throw new AdaFrameworkException(e);
		}
	}
	
	/**
	 * This method execute the databinding ralations. 
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public void bind(View pView) throws AdaFrameworkException {
		bind(pView, DataBinder.BINDING_ENTITY_TO_UI);
	}
	
	/**
	 * This method execute the databinding ralations. 
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public void bind(View pView, int pDirection) throws AdaFrameworkException {
		try {
			
			if (pView != null) {
				if (this.dataBinding == null) {
					loadDataBindings();
				}
				
				if (this.dataBinding != null) {
					if (this.dataBinding.size() > 0) {
						for(DataBinding binding : this.dataBinding) {
							View view = pView.findViewById(binding.ViewId);
							
							if (binding.Binder != null) {
								Object binder = binding.Binder.newInstance();
								if (binder instanceof DataBinder) {
									((DataBinder)binder).bind(binding, this, view, pDirection);
								}
							}
						}
						
						if (pDirection == DataBinder.BINDING_UI_TO_ENTITY) {
							if (this.getStatus() == STATUS_NOTHING) {
								this.setStatus(STATUS_UPDATED);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new AdaFrameworkException(e);
		}
	}

	
	/**
	 * This method execute data validation of the entidity definition.
	 * @param pResult List of validations that does not it's ok.
	 * @return True if the entity validation is OK.
	 * @throws com.mobandme.ada.exceptions.AdaFrameworkException
	 */
	public Boolean validate(Context pContext) throws AdaFrameworkException  {
		Boolean returnedValue = true;
		
		try {
			this.validationResult = new ArrayList<ValidationResult>();
			
			if (this.validations == null) {
				loadValidations(pContext);
			}
			
			if (this.validations != null) {
				if (this.validations.size() > 0) {
					for(Validation validation : this.validations) {
						Object fieldValue = null;
						
						try {
							
							if (validation.getterMethod != null) {
								fieldValue = validation.getterMethod.invoke(this, (Object[])null);
							} else {
								fieldValue = validation.EntityField.get(this);
							}
							
						} catch (IllegalAccessException e) {
							if (validation.getterMethod != null) {
								throw new InaccessibleFieldException(this.getClass().getName(), validation.EntityField.getName(), validation.getterMethod.getName());
							} else {
								throw new InaccessibleFieldException(this.getClass().getName(), validation.EntityField.getName(), "");
							}
						}
						
						if (validation.Validator != null) {
							Object validator = validation.Validator.newInstance();
							if (validator != null) {
								if (validator instanceof Validator) {
									Boolean result = ((Validator)validator).Validate(this, validation.EntityField, validation.Annotation, fieldValue);
									
									if (!result) {
										returnedValue = false;
										ValidationResult validationR = new ValidationResult();
										
										validationR.IsOK(false);
										validationR.setMessage(validation.message);
										validationR.setField(validation.EntityField);
										//this.validationResult.add(validationR);
										this.validationResult.add(0, validationR);
									}
								} else {
									ExceptionsHelper.manageException(new AdaFrameworkException(String.format("The validator %s does not extend of Validator.class", validation.Validator.getName())));
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(e);
		}
		
		return returnedValue;
	}
}
