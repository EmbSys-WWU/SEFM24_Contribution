package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations;

import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ComposableTransitionInformation;

public record TwoInformations<FirstInfoT extends ComposableTransitionInformation<FirstInfoT>, SecondInfoT extends ComposableTransitionInformation<SecondInfoT>>(
        FirstInfoT first, SecondInfoT second)
implements ComposableTransitionInformation<TwoInformations<FirstInfoT, SecondInfoT>> {

    public TwoInformations<FirstInfoT, SecondInfoT> setFirst(FirstInfoT first) {
        return new TwoInformations<>(first, this.second);
    }

    public TwoInformations<FirstInfoT, SecondInfoT> setSecond(SecondInfoT second) {
        return new TwoInformations<>(this.first, second);
    }

    @Override
    public TwoInformations<FirstInfoT, SecondInfoT> compose(TwoInformations<FirstInfoT, SecondInfoT> other) {
        return new TwoInformations<>(this.first.compose(other.first), this.second.compose(other.second));
    }

    @Override
    public TwoInformations<FirstInfoT, SecondInfoT> clone() {
        return new TwoInformations<>(this.first.clone(), this.second.clone());
    }

}
