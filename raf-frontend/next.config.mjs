/** @type {import('next').NextConfig} */

const nextConfig = {
    images: {
        remotePatterns: [
            {
                protocol: 'https',
                hostname: 'www.congress.gov',
                pathname: '/img/member/**',
            },
        ],
    },
};

export default nextConfig;
