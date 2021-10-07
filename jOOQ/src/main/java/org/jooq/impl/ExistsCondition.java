/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: http://www.jooq.org/licenses
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package org.jooq.impl;

import static org.jooq.Clause.CONDITION;
import static org.jooq.Clause.CONDITION_EXISTS;
import static org.jooq.impl.Keywords.K_EXISTS;
import static org.jooq.impl.Tools.visitSubquery;

import org.jooq.Clause;
import org.jooq.Context;
import org.jooq.Select;

/**
 * @author Lukas Eder
 */
final class ExistsCondition extends AbstractCondition {

    private static final long     serialVersionUID   = 5678338161136603292L;
    private static final Clause[] CLAUSES_EXISTS     = { CONDITION, CONDITION_EXISTS };

    private final Select<?>       query;

    ExistsCondition(Select<?> query) {
        this.query = query;
    }

    @Override
    final boolean isNullable() {
        return false;
    }

    @Override
    public final void accept(Context<?> ctx) {
        ctx.visit(K_EXISTS).sql(' ');
        visitSubquery(ctx, query, true);
    }

    @Override
    public final Clause[] clauses(Context<?> ctx) {
        return CLAUSES_EXISTS;
    }
}