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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.mobandme.ada.annotations.CustomValidation;
import com.mobandme.ada.annotations.Databinding;
import com.mobandme.ada.annotations.RangeValidation;
import com.mobandme.ada.annotations.RegularExpressionValidation;
import com.mobandme.ada.annotations.RequiredFieldValidation;
import com.mobandme.ada.annotations.TableField;
import com.mobandme.ada.exceptions.AdaFrameworkException;

/**
 * Entity ObjectSet.
 * @version 2.4.3
 * @author Mob&Me
 */
class ReflectionHelper {

	public static Method extractGetterMethod(Class<?> pObject, Field pField) {
		
		Method returnedValue = null;
		
		if (pField != null) {
			
			String getterMethodSuffix = "";
			
			TableField tableFieldAnnotation = pField.getAnnotation(TableField.class);
			if (tableFieldAnnotation != null) {
				if (tableFieldAnnotation.getterSuffix().trim() != "") {
					getterMethodSuffix = tableFieldAnnotation.getterSuffix(); 
				}
			} 

			if (getterMethodSuffix.equals("")) {
				Databinding dataBindAnnotation = pField.getAnnotation(Databinding.class);
				if (dataBindAnnotation != null) {
					if (!dataBindAnnotation.getterSuffix().trim().equals("")) {
						getterMethodSuffix = dataBindAnnotation.getterSuffix().trim();
					}
				} 
			}
			
			if (getterMethodSuffix.equals("")) {
				RequiredFieldValidation requiredFieldValidatorAnnotation = pField.getAnnotation(RequiredFieldValidation.class);
				if (requiredFieldValidatorAnnotation != null) {
					if (!requiredFieldValidatorAnnotation.getterSuffix().trim().equals("")) {
						getterMethodSuffix = requiredFieldValidatorAnnotation.getterSuffix().trim();
					}
				} 
			}
			
			if (getterMethodSuffix.equals("")) {
				RangeValidation rangeFieldValidatorAnnotation = pField.getAnnotation(RangeValidation.class);
				if (rangeFieldValidatorAnnotation != null) {
					if (!rangeFieldValidatorAnnotation.getterSuffix().trim().equals("")) {
						getterMethodSuffix = rangeFieldValidatorAnnotation.getterSuffix().trim();
					}
				} 
			}
			
			if (getterMethodSuffix.equals("")) {
				RegularExpressionValidation regularExpressionFieldValidatorAnnotation = pField.getAnnotation(RegularExpressionValidation.class);
				if (regularExpressionFieldValidatorAnnotation != null) {
					if (!regularExpressionFieldValidatorAnnotation.getterSuffix().trim().equals("")) {
						getterMethodSuffix = regularExpressionFieldValidatorAnnotation.getterSuffix().trim();
					}
				} 
			}
			
			
			if (getterMethodSuffix.equals("")) {
				CustomValidation customValidatorAnnotation = pField.getAnnotation(CustomValidation.class);
				if (customValidatorAnnotation != null) {
					if (!customValidatorAnnotation.getterSuffix().trim().equals("")) {
						getterMethodSuffix = customValidatorAnnotation.getterSuffix().trim();
					}
				} 
			}
			
			if (getterMethodSuffix.equals("")) {
				getterMethodSuffix = DataUtils.capitalize(pField.getName());
			}
			
			try {
				returnedValue = pObject.getMethod(String.format("get%s", getterMethodSuffix), (Class[])null);
			} catch (Exception e1) {
				try {
					returnedValue = pObject.getMethod(String.format("is%s", getterMethodSuffix), (Class[])null);
				} catch (Exception e2) {
					returnedValue = null;
				}
			}
		}
		
		return returnedValue;
	}
	
	public static Method extractSetterMethod(Class<?> pObject, Field pField) {
		
		Method returnedValue = null;
		
		if (pField != null) {
			
			String setterMethodSuffix = "";
			
			TableField tableFieldAnnotation = pField.getAnnotation(TableField.class);
			if (tableFieldAnnotation != null) {
				if (tableFieldAnnotation.setterSuffix().trim() != "") {
					setterMethodSuffix = tableFieldAnnotation.setterSuffix(); 
				}
			} 

			if (setterMethodSuffix.equals("")) {
				Databinding dataBindAnnotation = pField.getAnnotation(Databinding.class);
				if (dataBindAnnotation != null) {
					if (!dataBindAnnotation.setterSuffix().trim().equals("")) {
						setterMethodSuffix = dataBindAnnotation.setterSuffix().trim();
					}
				} 
			}
			
			if (setterMethodSuffix.equals("")) {
				RequiredFieldValidation requiredFieldValidatorAnnotation = pField.getAnnotation(RequiredFieldValidation.class);
				if (requiredFieldValidatorAnnotation != null) {
					if (!requiredFieldValidatorAnnotation.setterSuffix().trim().equals("")) {
						setterMethodSuffix = requiredFieldValidatorAnnotation.setterSuffix().trim();
					}
				} 
			}
			
			if (setterMethodSuffix.equals("")) {
				RangeValidation rangeFieldValidatorAnnotation = pField.getAnnotation(RangeValidation.class);
				if (rangeFieldValidatorAnnotation != null) {
					if (!rangeFieldValidatorAnnotation.getterSuffix().trim().equals("")) {
						setterMethodSuffix = rangeFieldValidatorAnnotation.getterSuffix().trim();
					}
				} 
			}
			
			if (setterMethodSuffix.equals("")) {
				RegularExpressionValidation regularExpressionFieldValidatorAnnotation = pField.getAnnotation(RegularExpressionValidation.class);
				if (regularExpressionFieldValidatorAnnotation != null) {
					if (!regularExpressionFieldValidatorAnnotation.getterSuffix().trim().equals("")) {
						setterMethodSuffix = regularExpressionFieldValidatorAnnotation.getterSuffix().trim();
					}
				} 
			}
			
			if (setterMethodSuffix.equals("")) {
				CustomValidation customValidatorAnnotation = pField.getAnnotation(CustomValidation.class);
				if (customValidatorAnnotation != null) {
					if (!customValidatorAnnotation.setterSuffix().trim().equals("")) {
						setterMethodSuffix = customValidatorAnnotation.setterSuffix().trim();
					}
				} 
			}
			
			if (setterMethodSuffix.equals("")) {
				setterMethodSuffix = DataUtils.capitalize(pField.getName());
			}
			
			try {
				returnedValue = pObject.getMethod(String.format("set%s", setterMethodSuffix), pField.getType());
			} catch (Exception e) {
				returnedValue = null;
			}
		}
		
		return returnedValue;
	}

	public static boolean extendsOf(Type pType, Class<?> pClass) throws AdaFrameworkException {
		boolean returnedValue = false;
		
		try {
			
			if ((Class<?>)pType == pClass) {
				returnedValue = true;
			} else {
				Class<?> superClass = ((Class<?>)pType).getSuperclass();
				while(superClass != null && superClass != Object.class) {
					if (superClass == pClass) {
						returnedValue = true;
						break;
					}
					superClass = superClass.getSuperclass();
				}
			}
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(e);
		}
		
		return returnedValue;
	}
}
