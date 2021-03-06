/**
 * Copyright (C) 2010-16 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rvesse.airline.restrictions.factories;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import com.github.rvesse.airline.annotations.restrictions.AllowedValues;
import com.github.rvesse.airline.restrictions.ArgumentsRestriction;
import com.github.rvesse.airline.restrictions.OptionRestriction;
import com.github.rvesse.airline.restrictions.common.AllowedRawValuesRestriction;
import com.github.rvesse.airline.restrictions.common.AllowedValuesRestriction;

public class AllowedValuesRestrictionFactory implements OptionRestrictionFactory, ArgumentsRestrictionFactory {

    @Override
    public OptionRestriction createOptionRestriction(Annotation annotation) {
        return (OptionRestriction) createCommon(annotation);
    }

    protected Object createCommon(Annotation annotation) {
        if (annotation instanceof AllowedRawValues) {
            AllowedRawValues allowedValues = (AllowedRawValues) annotation;
            return new AllowedRawValuesRestriction(allowedValues.ignoreCase(),
                    Locale.forLanguageTag(allowedValues.locale()), allowedValues.allowedValues());
        } else if (annotation instanceof AllowedValues) {
            AllowedValues allowedValues = (AllowedValues) annotation;
            return new AllowedValuesRestriction(allowedValues.allowedValues());
        }

        return null;
    }

    @Override
    public ArgumentsRestriction createArgumentsRestriction(Annotation annotation) {
        return (ArgumentsRestriction) createCommon(annotation);
    }

    @Override
    public List<Class<? extends Annotation>> supportedOptionAnnotations() {
        return supportedAnnotations();
    }

    protected List<Class<? extends Annotation>> supportedAnnotations() {
        List<Class<? extends Annotation>> supported = new ArrayList<>();
        supported.add(AllowedRawValues.class);
        supported.add(AllowedValues.class);
        return supported;
    }

    @Override
    public List<Class<? extends Annotation>> supportedArgumentsAnnotations() {
        return supportedAnnotations();
    }
}
