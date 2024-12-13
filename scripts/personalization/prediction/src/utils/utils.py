def stratify_by_treatment(X, treatment_col, encoded_columns):
    """
    Create a stratification label based on treatment type.

    Args:
        X: Input feature DataFrame.
        treatment_col: The column name representing treatments.
        encoded_columns: Dictionary of encoded column mappings.

    Returns:
        A Series of stratification labels.
    """
    if treatment_col in encoded_columns:
        treatment_col_encoded = encoded_columns[treatment_col]
        if len(treatment_col_encoded) == 1:
            treatment_data = X[treatment_col_encoded[0]]
        else:
            # For one-hot encoded treatments, get the column with the maximum value
            treatment_data = X[treatment_col_encoded].idxmax(axis=1).apply(
                lambda x: x.replace(f"{treatment_col}_", "")
            )
    else:
        treatment_data = X[treatment_col]

    return treatment_data.astype(str)


