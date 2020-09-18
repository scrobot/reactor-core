/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.core.publisher;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.test.subscriber.AssertSubscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MonoCountTest {

	@Test
	public void sourceNull() {
		assertThrows(NullPointerException.class, () ->
				new MonoCount<>(null));
	}

	public void normal() {
		AssertSubscriber<Long> ts = AssertSubscriber.create();

		Flux.range(1, 10).count().subscribe(ts);

		ts.assertValues(10L)
		  .assertComplete()
		  .assertNoError();
	}

	public void normalBackpressured() {
		AssertSubscriber<Long> ts = AssertSubscriber.create(0);

		Flux.range(1, 10).count().subscribe(ts);

		ts.assertNoValues()
		  .assertNotComplete()
		  .assertNoError();

		ts.request(2);

		ts.assertValues(10L)
		  .assertComplete()
		  .assertNoError();
	}

	@Test
	public void scanCountSubscriber() {
		CoreSubscriber<Long> actual = new LambdaMonoSubscriber<>(null, e -> {}, null, null);
		MonoCount.CountSubscriber<String> test = new MonoCount.CountSubscriber<>(actual);
		Subscription parent = Operators.emptySubscription();
		test.onSubscribe(parent);

		assertThat(test.scan(Scannable.Attr.PREFETCH)).isEqualTo(Integer.MAX_VALUE);

		assertThat(test.scan(Scannable.Attr.PARENT)).isSameAs(parent);
		assertThat(test.scan(Scannable.Attr.ACTUAL)).isSameAs(actual);

		//only TERMINATED state evaluated is one from Operators: hasValue
		assertThat(test.scan(Scannable.Attr.TERMINATED)).isFalse();
		test.onComplete();
		assertThat(test.scan(Scannable.Attr.TERMINATED)).isTrue();

		assertThat(test.scan(Scannable.Attr.CANCELLED)).isFalse();
		test.cancel();
		assertThat(test.scan(Scannable.Attr.CANCELLED)).isTrue();
	}

}
