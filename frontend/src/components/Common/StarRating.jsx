import * as React from 'react';
import Rating from '@mui/material/Rating';
import { styled } from '@mui/material/styles';

const StyledRating = styled(Rating)({
    '& .MuiRating-iconFilled': {
        color: '#7fb5ff',
    },
    '& .MuiRating-iconHover': { 
        transform: 'none',
        transition: 'none',
    },
    '& .MuiRating-iconActive': {
        transform: 'none',
        transition: 'none',
    },
    '& .MuiRating-icon': {
        transition: 'none',
    },
});

export default function StarRating({ rating, onRate, readOnly = false, size = 'small' }) {
    const handleChange = (event, newValue) => {
        if (!readOnly && onRate) {
            onRate(newValue); // 부모의 setRating(newValue) 호출
        }
    };

    return (
        <StyledRating
            name="rating"
            value={rating}
            precision={0.5}
            readOnly={readOnly}
            onChange={handleChange}
            size={size} // 'small', 'medium', 'large' 등으로 크기 조절 가능
        />
    );
}