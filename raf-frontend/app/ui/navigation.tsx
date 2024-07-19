'use client';

import { Popover, Transition } from '@headlessui/react';
import { Fragment, useState } from 'react';
import { Bars3Icon, XMarkIcon } from '@heroicons/react/24/outline';
import Link from 'next/link';

const links = [
    { name: 'Home', href: '/' },
    {
        name: 'Congress',
        dropdown: [
            { name: 'Politicians', href: '/congress/politicians' },
            { name: 'Bills', href: '/congress/bills' },
            { name: 'Laws', href: '/congress/laws' },
            { name: 'Amendments', href: '/congress/amendments' },
            { name: 'Committees', href: '/congress/committees' },
        ],
    },
];

export default function Navigation() {
    const [isCongressOpen, setIsCongressOpen] = useState(false);

    return (
        <Popover className="container mx-auto flex items-center border-b-2 px-6 py-2 h-24">
            <h1 className="font-bold">Rank and File</h1>
            <div className="grow">
                <div className="hidden sm:flex items-center justify-center gap-2 md:gap-8">
                    {links.map((link) =>
                        link.dropdown ? (
                            <Popover key={link.name} className="relative">
                                <Popover.Button className="inline-flex items-center">
                                    {link.name}
                                </Popover.Button>
                                <Transition
                                    as={Fragment}
                                    enter="transition ease-out duration-200"
                                    enterFrom="opacity-0 translate-y-1"
                                    enterTo="opacity-100 translate-y-0"
                                    leave="transition ease-in duration-150"
                                    leaveFrom="opacity-100 translate-y-0"
                                    leaveTo="opacity-0 translate-y-1"
                                >
                                    <Popover.Panel className="absolute z-10 mt-3 w-48 bg-white shadow-lg ring-1 ring-black ring-opacity-5">
                                        <div className="rounded-lg ring-1 ring-black ring-opacity-5 divide-y-2 divide-gray-50">
                                            <div className="py-1">
                                                {link.dropdown.map((sublink) => (
                                                    <Link
                                                        key={sublink.name}
                                                        href={sublink.href}
                                                        className="block px-4 py-2 text-sm"
                                                    >
                                                        {sublink.name}
                                                    </Link>
                                                ))}
                                            </div>
                                        </div>
                                    </Popover.Panel>
                                </Transition>
                            </Popover>
                        ) : (
                            <Link key={link.name} href={link.href}>
                                {link.name}
                            </Link>
                        )
                    )}
                </div>
            </div>

            <div className="flex grow items-center justify-end sm:hidden">
                <Popover.Button className="inline-flex items-center justify-center rounded-md bg-white p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500">
                    <span className="sr-only">Open menu</span>
                    <Bars3Icon className="h-6 w-6" aria-hidden="true" />
                </Popover.Button>
            </div>

            <Transition
                as={Fragment}
                enter="duration-200 ease-out"
                enterFrom="opacity-0 scale-95"
                enterTo="opacity-100 scale-100"
                leave="duration-100 ease-in"
                leaveFrom="opacity-100 scale-100"
                leaveTo="opacity-0 scale-95"
            >
                <Popover.Panel focus className="absolute inset-x-0 top-0 origin-top-right transform p-2 transition md:hidden">
                    <div className="rounded-lg bg-white shadow-lg ring-1 ring-black ring-opacity-5 divide-y-2 divide-gray-50">
                        <div className="px-5 pt-5 pb-6">
                            <div className="flex items-center justify-between">
                                <h1 className="font-bold">Rank and File</h1>
                                <div className="-mr-2">
                                    <Popover.Button className="inline-flex items-center justify-center rounded-md bg-white p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500">
                                        <span className="sr-only">Close menu</span>
                                        <XMarkIcon className="h-6 w-6" aria-hidden="true" />
                                    </Popover.Button>
                                </div>
                            </div>
                            <div className="mt-6">
                                <nav className="grid gap-y-8">
                                    {links.map((link) =>
                                        link.dropdown ? (
                                            <Fragment key={link.name}>
                                                <button
                                                    className="focus:outline-none focus:ring-2 focus:ring-inset focus:ring-gray-500 px-2 text-left"
                                                    onClick={() => setIsCongressOpen(!isCongressOpen)}
                                                >
                                                    {link.name}
                                                </button>
                                                {isCongressOpen && (
                                                    <div className="ml-4 space-y-2">
                                                        {link.dropdown.map((sublink) => (
                                                            <Link
                                                                key={sublink.name}
                                                                href={sublink.href}
                                                                className="block px-4 py-2 text-sm"
                                                            >
                                                                {sublink.name}
                                                            </Link>
                                                        ))}
                                                    </div>
                                                )}
                                            </Fragment>
                                        ) : (
                                            <Link
                                                key={link.name}
                                                href={link.href}
                                                className="focus:outline-none focus:ring-2 focus:ring-inset focus:ring-gray-500 px-2"
                                            >
                                                {link.name}
                                            </Link>
                                        )
                                    )}
                                </nav>
                            </div>
                            <div className="mt-6 flex flex-col items-center gap-2">
                                <Link
                                    href="register"
                                    className="rounded-md bg-white px-4 py-2 text-sm font-medium text-black md:text-xl w-full border-2 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-gray-500"
                                >
                                    Register
                                </Link>
                                <Link
                                    href="login"
                                    className="rounded-md bg-gray-500 px-4 py-2 text-sm font-medium md:text-xl w-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-gray-500"
                                >
                                    Login
                                </Link>
                            </div>
                        </div>
                    </div>
                </Popover.Panel>
            </Transition>

            <div className="hidden sm:block">
                <Link href="login" className="mr-2 font-bold">
                    Login
                </Link>
                <Link href="register" className="font-bold">
                    Register
                </Link>
            </div>
        </Popover>
    );
}
