import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// CAMBIO: limpiar el DOM entre tests para evitar acumulación de renderizados.
afterEach(() => {
	cleanup();
});
